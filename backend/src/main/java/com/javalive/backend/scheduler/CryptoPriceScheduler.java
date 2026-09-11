package com.javalive.backend.scheduler;

import com.javalive.backend.entity.Instrument;
import com.javalive.backend.repository.InstrumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Java port of the source app's {@code update:crypto} command (public CoinGecko {@code /coins/markets}
 * endpoint, no API key required) — scheduled {@code ->everyTenMinutes()} in Kernel.php.
 */
@Component
public class CryptoPriceScheduler {

    private static final Logger log = LoggerFactory.getLogger(CryptoPriceScheduler.class);
    private static final String URL = "https://api.coingecko.com/api/v3/coins/markets"
            + "?vs_currency=usd&order=market_cap_desc&per_page=50&page=1&sparkline=false";
    private static final ParameterizedTypeReference<List<Map<String, Object>>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {};

    private final InstrumentRepository instrumentRepository;
    private final RestClient restClient = RestClient.create();

    public CryptoPriceScheduler(InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

    @Scheduled(initialDelay = 15_000, fixedDelay = 600_000)
    public void updateCryptoPrices() {
        List<Map<String, Object>> data;
        try {
            data = restClient.get().uri(URL).retrieve().body(RESPONSE_TYPE);
        } catch (RestClientException e) {
            log.warn("UpdateCryptoPrices: failed to fetch data from CoinGecko: {}", e.getMessage());
            return;
        }
        if (data == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        int count = 0;
        for (Map<String, Object> item : data) {
            try {
                String symbol = String.valueOf(item.get("symbol")).toUpperCase() + "/USD";
                Instrument instrument = instrumentRepository.findBySymbol(symbol).orElseGet(Instrument::new);
                instrument.setSymbol(symbol);
                instrument.setCoingeckoId(str(item.get("id")));
                instrument.setName(str(item.get("name")));
                instrument.setType("crypto");
                instrument.setHighPrice(num(item.get("high_24h")));
                instrument.setLowPrice(num(item.get("low_24h")));
                instrument.setClosePrice(num(item.get("current_price")));
                instrument.setPrice(num(item.get("current_price")));
                instrument.setPercentChange24h(num(item.get("price_change_percentage_24h")));
                instrument.setChangeAmount(num(item.get("price_change_24h")));
                instrument.setMarketCap(num(item.get("market_cap")));
                instrument.setVolume(num(item.get("total_volume")));
                instrument.setLogo(str(item.get("image")));
                if (instrument.getCreatedAt() == null) {
                    instrument.setCreatedAt(now);
                }
                instrument.setUpdatedAt(now);
                instrumentRepository.save(instrument);
                count++;
            } catch (Exception e) {
                log.error("UpdateCryptoPrices: failed to update instrument {}: {}", item.get("symbol"), e.getMessage());
            }
        }
        log.info("Crypto prices updated successfully. ({} records)", count);
    }

    private String str(Object value) {
        return value != null ? String.valueOf(value) : null;
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
