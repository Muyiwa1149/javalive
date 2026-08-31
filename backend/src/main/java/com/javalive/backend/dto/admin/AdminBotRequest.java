package com.javalive.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record AdminBotRequest(
        @NotBlank String name,
        @NotBlank String botType,
        @NotBlank @Size(min = 50) String description,
        @NotNull BigDecimal minInvestment,
        @NotNull BigDecimal maxInvestment,
        @NotNull BigDecimal dailyProfitMin,
        @NotNull BigDecimal dailyProfitMax,
        @NotNull Integer successRate,
        @NotNull Integer durationDays,
        @NotNull List<String> tradingPairs,
        @NotBlank String status
) {
}
