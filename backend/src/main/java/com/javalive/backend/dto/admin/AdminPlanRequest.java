package com.javalive.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AdminPlanRequest(
        @NotBlank String name,
        String tag,
        @NotNull BigDecimal price,
        @NotNull BigDecimal minPrice,
        @NotNull BigDecimal maxPrice,
        BigDecimal minReturnPct,
        BigDecimal maxReturnPct,
        BigDecimal gift,
        String expectedReturn,
        String incrementType,
        String incrementInterval,
        BigDecimal incrementAmount,
        Integer expirationDays
) {
}
