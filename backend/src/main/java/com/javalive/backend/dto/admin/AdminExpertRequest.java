package com.javalive.backend.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AdminExpertRequest(
        @NotBlank String name,
        String tag,
        @NotNull @Min(1) @Max(5) Integer rating,
        @NotNull BigDecimal equity,
        @NotNull BigDecimal totalProfit,
        @NotNull @Min(0) @Max(100) Integer winRate,
        @NotNull @Min(0) Integer totalTrades,
        @NotNull BigDecimal price,
        String description,
        @NotBlank String status
) {
}
