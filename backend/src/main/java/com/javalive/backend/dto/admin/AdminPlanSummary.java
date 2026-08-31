package com.javalive.backend.dto.admin;

import com.javalive.backend.entity.Plan;

import java.math.BigDecimal;

public record AdminPlanSummary(
        Long id, String name, String tag, String type, BigDecimal price, BigDecimal minPrice, BigDecimal maxPrice,
        BigDecimal minReturnPct, BigDecimal maxReturnPct, BigDecimal gift, String expectedReturn,
        String incrementType, String incrementInterval, BigDecimal incrementAmount, Integer expirationDays,
        boolean active
) {
    public static AdminPlanSummary from(Plan p) {
        return new AdminPlanSummary(p.getId(), p.getName(), p.getTag(), p.getType(), p.getPrice(), p.getMinPrice(),
                p.getMaxPrice(), p.getMinReturnPct(), p.getMaxReturnPct(), p.getGift(), p.getExpectedReturn(),
                p.getIncrementType(), p.getIncrementInterval(), p.getIncrementAmount(), p.getExpirationDays(),
                Boolean.TRUE.equals(p.getActive()));
    }
}
