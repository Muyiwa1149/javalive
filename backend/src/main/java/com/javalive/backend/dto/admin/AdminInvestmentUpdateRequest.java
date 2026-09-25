package com.javalive.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Full admin edit of an investment — every field a human might legitimately need to correct. */
public record AdminInvestmentUpdateRequest(
        Long planId,
        @NotNull BigDecimal amount,
        @NotBlank String active,
        String invDuration,
        LocalDateTime activatedAt,
        LocalDateTime expireDate,
        LocalDateTime lastGrowth,
        @NotNull BigDecimal profitEarned,
        @NotNull BigDecimal profitWithdrawn,
        boolean withdrawalDisabled
) {
}
