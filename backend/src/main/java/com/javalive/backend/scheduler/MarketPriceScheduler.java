package com.javalive.backend.scheduler;

import com.javalive.backend.entity.Instrument;
import com.javalive.backend.entity.MarketPrice;
import com.javalive.backend.repository.InstrumentRepository;
import com.javalive.backend.repository.MarketPriceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

/**
 * Java port of the source app's {@code market:prices} command — historical OHLC candle capture into
 * the {@code market_prices} table (distinct from {@code instruments}, the "current ticker snapshot"
 * table {@link CryptoPriceScheduler}/{@link MarketInstrumentScheduler} maintain). Scheduled
 * {@code ->everyFiveMinutes()->withoutOverlapping()} in Kernel.php.
 *
 * <p><b>Known source quirk, carried over as-is</b>: for non-crypto instruments this calls Finnhub's
 * {@code /stock/candle} using the raw {@code instrument.symbol} (e.g. "EUR/USD"), without the
 * asset-type symbol reformatting ({@code OANDA:EUR_USD} etc.) that the sibling {@code update:market}
 * command applies — so forex/index/commodity candles will typically come back empty ({@code s != "ok"})
 * and only get logged, same as source. Not fixed here since it's silent, non-breaking behavior
 * identical to what's live today, not a visible feature gap.
 */
@Component
public class MarketPriceScheduler {

    private static final Logger log = LoggerFactory.getLogger(MarketPriceScheduler.class);
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE = new ParameterizedTypeReference<>() {};

    private final InstrumentRepository instrumentRepository;
    private final MarketPriceRepository marketPriceRepository;
    private final RestClient restClient = RestClient.create();

    @Value("${javalive.finnhub.api-key:}")
    private String finnhubApiKey;

    public MarketPriceScheduler(InstrumentRepository instrumentRepository, MarketPriceRepository marketPriceRepository) {
        this.instrumentRepository = instrumentRepository;
        this.marketPriceRepository = marketPriceRepository;
    }

    @Scheduled(initialDelay = 20_000, fixedDelay = 300_000)
    public void fetchMarketPrices() {
        List<Instrument> instruments = instrumentRepository.findAll();
        int success = 0;
        int failed = 0;

        for (Instrument instrument : instruments) {
            try {
                if ("crypto".equals(instrument.getType())) {
                    updateCrypto(instrument);
                } else {
                    updateFromFinnhub(instrument);
                }
                success++;
                Thread.sleep(300);
            } catch (Exception e) {
                failed++;
                log.error("FetchMarketPrices failed for {}: {}", instrument.getSymbol(), e.getMessage());
            }
        }

        log.info("Market prices updated. Success: {}, Failed: {}", success, failed);
    }

    private void updateCrypto(Instrument instrument) {
        if (instrument.getCoingeckoId() == null || instrument.getCoingeckoId().isBlank()) {
            log.warn("Missing coingecko_id for {}", instrument.getSymbol());
            return;
        }

        Map<String, Object> response;
        try {
            response = restClient.get()
                    .uri("https://api.coingecko.com/api/v3/simple/price?ids={id}&vs_currencies=usd"
                            + "&include_market_cap=true&include_24hr_vol=true&include_24hr_change=true&include_last_updated_at=true",
                            instrument.getCoingeckoId())
                    .retrieve().body(MAP_TYPE);
        } catch (Exception e) {
            log.error("Coingecko API error for {}: {}", instrument.getSymbol(), e.getMessage());
            return;
        }
        if (response == null) return;

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.get(instrument.getCoingeckoId());
        if (data == null || data.get("usd") == null) {
            log.warn("No data for {}", instrument.getSymbol());
            return;
        }

        BigDecimal price = num(data.get("usd"));
        LocalDateTime timestamp = LocalDateTime.now().withSecond(0).withNano(0);

        upsert(instrument, timestamp, price, price, price, price, num(data.get("usd_24h_vol")), "coingecko");

        instrument.setPrice(price);
        instrument.setUpdatedAt(LocalDateTime.now());
        instrumentRepository.save(instrument);
    }

    @SuppressWarnings("unchecked")
    private void updateFromFinnhub(Instrument instrument) {
        if (finnhubApiKey == null || finnhubApiKey.isBlank()) {
            return;
        }

        long from = Instant.now().minusSeconds(300).getEpochSecond();
        long to = Instant.now().getEpochSecond();

        Map<String, Object> response;
        try {
            response = restClient.get()
                    .uri("https://finnhub.io/api/v1/stock/candle?symbol={symbol}&resolution=1&from={from}&to={to}&token={token}",
                            instrument.getSymbol(), from, to, finnhubApiKey)
                    .retrieve().body(MAP_TYPE);
        } catch (Exception e) {
            log.warn("No data from Finnhub for {}: {}", instrument.getSymbol(), e.getMessage());
            return;
        }
        if (response == null || !"ok".equals(response.get("s"))) {
            log.warn("No data from Finnhub for {}", instrument.getSymbol());
            return;
        }

        List<Number> timestamps = (List<Number>) response.get("t");
        List<Number> opens = (List<Number>) response.get("o");
        List<Number> highs = (List<Number>) response.get("h");
        List<Number> lows = (List<Number>) response.get("l");
        List<Number> closes = (List<Number>) response.get("c");
        List<Number> volumes = (List<Number>) response.get("v");
        if (timestamps == null) return;

        BigDecimal latestClose = null;
        for (int i = 0; i < timestamps.size(); i++) {
            LocalDateTime ts = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamps.get(i).longValue()), ZoneId.systemDefault())
                    .withSecond(0).withNano(0);
            BigDecimal open = num(opens.get(i));
            BigDecimal high = num(highs.get(i));
            BigDecimal low = num(lows.get(i));
            BigDecimal close = num(closes.get(i));
            BigDecimal volume = volumes != null ? num(volumes.get(i)) : null;

            upsert(instrument, ts, open, high, low, close, volume, "finnhub");
            latestClose = close;
        }

        if (latestClose != null) {
            instrument.setPrice(latestClose);
            instrument.setUpdatedAt(LocalDateTime.now());
            instrumentRepository.save(instrument);
        }
    }

    private void upsert(Instrument instrument, LocalDateTime timestamp, BigDecimal open, BigDecimal high,
                         BigDecimal low, BigDecimal close, BigDecimal volume, String source) {
        MarketPrice price = marketPriceRepository
                .findByInstrumentIdAndRecordedAtAndPriceInterval(instrument.getId(), timestamp, "1m")
                .orElseGet(MarketPrice::new);

        LocalDateTime now = LocalDateTime.now();
        price.setInstrument(instrument);
        price.setRecordedAt(timestamp);
        price.setPriceInterval("1m");
        price.setOpenPrice(open);
        price.setHighPrice(high);
        price.setLowPrice(low);
        price.setClosePrice(close);
        price.setVolume(volume);
        price.setSource(source);
        if (price.getCreatedAt() == null) {
            price.setCreatedAt(now);
        }
        price.setUpdatedAt(now);
        marketPriceRepository.save(price);
    }

    private BigDecimal num(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
