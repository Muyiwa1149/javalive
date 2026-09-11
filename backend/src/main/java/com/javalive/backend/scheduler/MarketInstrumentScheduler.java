package com.javalive.backend.scheduler;

import com.javalive.backend.entity.Instrument;
import com.javalive.backend.repository.InstrumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java port of the source app's {@code update:market} command — updates the {@code instruments}
 * "current ticker" table for stocks/forex/indices/commodities from Finnhub. Scheduled
 * {@code ->everyTenMinutes()} in Kernel.php. CLI-only concerns from the source command (progress bar,
 * chunk-size option, verbose flags) are dropped since this runs headless; the actual behavior
 * (symbol reformatting per asset type, 429 backoff-and-retry-once, stock profile/candle enrichment,
 * fallback to existing data on failure) is preserved.
 */
@Component
public class MarketInstrumentScheduler {

    private static final Logger log = LoggerFactory.getLogger(MarketInstrumentScheduler.class);
    private static final Pattern SIX_LETTER = Pattern.compile("^[A-Z]{6}$");
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE = new ParameterizedTypeReference<>() {};

    private static final Map<String, String> INDEX_MAP = Map.ofEntries(
            Map.entry("GSPC", "SPX"), Map.entry("^GSPC", "SPX"), Map.entry("SPX", "SPX"),
            Map.entry("DJI", "DJI"), Map.entry("^DJI", "DJI"),
            Map.entry("IXIC", "NDX"), Map.entry("^IXIC", "NDX"), Map.entry("NDX", "NDX"),
            Map.entry("FTSE", "FTSE:FTSE"), Map.entry("^FTSE", "FTSE:FTSE"),
            Map.entry("GDAXI", "XETR:DAX"), Map.entry("^GDAXI", "XETR:DAX"),
            Map.entry("FCHI", "EURONEXT:PX1"), Map.entry("^FCHI", "EURONEXT:PX1")
    );
    private static final Map<String, String> COMMODITY_MAP = Map.of(
            "XAUUSD", "OANDA:XAU_USD", "XAGUSD", "OANDA:XAG_USD",
            "XBRUSD", "OANDA:BCO_USD", "XTIUSD", "OANDA:WTI_USD"
    );

    private final InstrumentRepository instrumentRepository;
    private final RestClient restClient = RestClient.create();

    @Value("${javalive.finnhub.api-key:}")
    private String apiKey;

    public MarketInstrumentScheduler(InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

    @Scheduled(initialDelay = 25_000, fixedDelay = 600_000)
    public void updateMarketInstruments() {
        if (apiKey == null || apiKey.isBlank()) {
            return;
        }

        List<Instrument> symbols = symbolsToUpdate();
        int success = 0;
        int failed = 0;

        for (Instrument s : symbols) {
            try {
                processSymbol(s);
                success++;
                Thread.sleep(1200);
            } catch (Exception e) {
                failed++;
                log.error("Failed to update {}: {}", s.getSymbol(), e.getMessage());
            }
        }

        log.info("Market instruments update finished. Success: {}, Failed: {}", success, failed);
    }

    /** Source falls back to a hardcoded default list when {@code instruments} is empty; here that
     *  list is only reached pre-Phase-1-seed since {@code instruments} rows already exist from crypto sync. */
    private List<Instrument> symbolsToUpdate() {
        List<Instrument> nonCrypto = instrumentRepository.findAll().stream()
                .filter(i -> !"crypto".equals(i.getType())).toList();
        if (!nonCrypto.isEmpty()) {
            return nonCrypto;
        }
        return List.of(
                stub("AAPL", "stock"), stub("MSFT", "stock"), stub("AMZN", "stock"), stub("GOOGL", "stock"),
                stub("TSLA", "stock"), stub("META", "stock"), stub("NVDA", "stock"),
                stub("^GSPC", "index"), stub("^DJI", "index"), stub("^IXIC", "index"),
                stub("^FTSE", "index"), stub("^GDAXI", "index"),
                stub("EUR/USD", "forex"), stub("GBP/USD", "forex"), stub("USD/JPY", "forex"), stub("USD/CHF", "forex"),
                stub("XAUUSD", "commodity"), stub("XAGUSD", "commodity"), stub("XBRUSD", "commodity")
        );
    }

    private Instrument stub(String symbol, String type) {
        return Instrument.builder().symbol(symbol).type(type).name(symbol).build();
    }

    private void processSymbol(Instrument s) {
        String apiSymbol = formatSymbolForApi(s.getSymbol(), s.getType());

        Map<String, Object> quote = fetchQuote(apiSymbol);
        if (quote == null && "index".equals(s.getType())) {
            String alternate = apiSymbol.replace("I:", "").replace("^", "");
            quote = fetchQuote(alternate);
        }

        Instrument existing = instrumentRepository.findBySymbol(s.getSymbol()).orElse(null);

        if (quote == null || quote.get("c") == null) {
            if (existing != null) {
                existing.setUpdatedAt(LocalDateTime.now());
                instrumentRepository.save(existing);
            }
            return;
        }

        BigDecimal price = num(quote.get("c"));
        BigDecimal previousClose = num(quote.get("pc"));
        if ((price == null || price.signum() == 0) && previousClose != null && previousClose.signum() > 0) {
            price = previousClose;
        }

        Instrument instrument = existing != null ? existing : new Instrument();
        instrument.setSymbol(s.getSymbol());
        instrument.setType(s.getType());
        if (instrument.getName() == null) {
            instrument.setName(s.getSymbol());
        }
        instrument.setOpenPrice(quote.get("o") != null ? num(quote.get("o")) : price);
        instrument.setHighPrice(quote.get("h") != null ? num(quote.get("h")) : price);
        instrument.setLowPrice(quote.get("l") != null ? num(quote.get("l")) : price);
        instrument.setClosePrice(previousClose != null ? previousClose : price);
        instrument.setPrice(price);
        if (previousClose != null && previousClose.signum() > 0 && price != null) {
            instrument.setChangeAmount(price.subtract(previousClose));
            instrument.setPercentChange24h(price.subtract(previousClose)
                    .divide(previousClose, 6, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
        }

        if ("stock".equals(s.getType())) {
            enrichStockProfile(instrument, apiSymbol);
        }

        LocalDateTime now = LocalDateTime.now();
        if (instrument.getCreatedAt() == null) {
            instrument.setCreatedAt(now);
        }
        instrument.setUpdatedAt(now);
        instrumentRepository.save(instrument);
    }

    private void enrichStockProfile(Instrument instrument, String apiSymbol) {
        try {
            Thread.sleep(1200);
            Map<String, Object> profile = restClient.get()
                    .uri("https://finnhub.io/api/v1/stock/profile2?symbol={symbol}&token={token}", apiSymbol, apiKey)
                    .retrieve().body(MAP_TYPE);
            if (profile != null) {
                if (profile.get("logo") != null) instrument.setLogo(String.valueOf(profile.get("logo")));
                if (profile.get("marketCapitalization") != null) instrument.setMarketCap(num(profile.get("marketCapitalization")));
                if (profile.get("name") != null) instrument.setName(String.valueOf(profile.get("name")));
            }

            Thread.sleep(1200);
            long to = java.time.Instant.now().getEpochSecond();
            long from = java.time.Instant.now().minusSeconds(86_400).getEpochSecond();
            Map<String, Object> candle = restClient.get()
                    .uri("https://finnhub.io/api/v1/stock/candle?symbol={symbol}&resolution=D&from={from}&to={to}&token={token}",
                            apiSymbol, from, to, apiKey)
                    .retrieve().body(MAP_TYPE);
            if (candle != null) {
                @SuppressWarnings("unchecked")
                List<Number> volumes = (List<Number>) candle.get("v");
                if (volumes != null && !volumes.isEmpty()) {
                    instrument.setVolume(BigDecimal.valueOf(volumes.get(volumes.size() - 1).doubleValue()));
                }
            }
        } catch (Exception e) {
            log.warn("Stock profile/candle enrichment failed for {}: {}", apiSymbol, e.getMessage());
        }
    }

    private Map<String, Object> fetchQuote(String apiSymbol) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri("https://finnhub.io/api/v1/quote?symbol={symbol}&token={token}", apiSymbol, apiKey)
                    .retrieve().body(MAP_TYPE);
            return response;
        } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
            try {
                Thread.sleep(60_000);
                return restClient.get()
                        .uri("https://finnhub.io/api/v1/quote?symbol={symbol}&token={token}", apiSymbol, apiKey)
                        .retrieve().body(MAP_TYPE);
            } catch (Exception retryEx) {
                log.warn("Retry after rate limit failed for {}: {}", apiSymbol, retryEx.getMessage());
                return null;
            }
        } catch (Exception e) {
            log.warn("Finnhub quote error for {}: {}", apiSymbol, e.getMessage());
            return null;
        }
    }

    String formatSymbolForApi(String symbol, String type) {
        return switch (type) {
            case "forex" -> formatForex(symbol);
            case "index" -> formatIndex(symbol);
            case "crypto" -> formatCrypto(symbol);
            case "commodity" -> COMMODITY_MAP.getOrDefault(symbol, symbol);
            default -> symbol;
        };
    }

    private String formatForex(String symbol) {
        if (symbol.startsWith("OANDA:")) return symbol;
        String s = symbol;
        if (s.contains(":")) {
            s = s.split(":", 2)[1];
        }
        if (s.contains("/")) {
            String[] parts = s.split("/");
            if (parts.length == 2) return "OANDA:" + parts[0] + "_" + parts[1];
        }
        if (s.contains("_")) {
            return "OANDA:" + s;
        }
        Matcher m = SIX_LETTER.matcher(s);
        if (m.matches()) {
            return "OANDA:" + s.substring(0, 3) + "_" + s.substring(3, 6);
        }
        return "OANDA:" + s;
    }

    private String formatIndex(String symbol) {
        if (INDEX_MAP.containsKey(symbol)) return INDEX_MAP.get(symbol);
        return symbol.startsWith("^") ? symbol.substring(1) : symbol;
    }

    private String formatCrypto(String symbol) {
        if (symbol.contains("/")) {
            String[] parts = symbol.split("/");
            if (parts.length == 2) return "COINBASE:" + parts[0] + "-" + parts[1];
        }
        return "BINANCE:" + symbol;
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
