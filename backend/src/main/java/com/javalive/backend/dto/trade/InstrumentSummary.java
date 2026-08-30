package com.javalive.backend.dto.trade;

import com.javalive.backend.entity.Instrument;

import java.math.BigDecimal;

public record InstrumentSummary(
        Long id, String symbol, String name, String type, BigDecimal price, BigDecimal percentChange24h,
        BigDecimal marketCap, BigDecimal volume, String logo
) {
    public static InstrumentSummary from(Instrument i) {
        return new InstrumentSummary(i.getId(), i.getSymbol(), i.getName(), i.getType(), i.getPrice(),
                i.getPercentChange24h(), i.getMarketCap(), i.getVolume(), i.getLogo());
    }
}
