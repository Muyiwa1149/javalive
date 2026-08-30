package com.javalive.backend.dto.plan;

import com.javalive.backend.entity.Plan;

import java.math.BigDecimal;

public record PlanSummary(
        Long id, String name, String tag, String type, BigDecimal price, BigDecimal minPrice, BigDecimal maxPrice,
        BigDecimal minReturnPct, BigDecimal maxReturnPct, BigDecimal gift, String incrementInterval,
        String incrementType, BigDecimal incrementAmount, Integer expirationDays
) {
    public static PlanSummary from(Plan p) {
        return new PlanSummary(
                p.getId(), p.getName(), p.getTag(), p.getType(), p.getPrice(), p.getMinPrice(), p.getMaxPrice(),
                p.getMinReturnPct(), p.getMaxReturnPct(), p.getGift(), p.getIncrementInterval(),
                p.getIncrementType(), p.getIncrementAmount(), p.getExpirationDays()
        );
    }
}
