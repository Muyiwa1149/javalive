package com.javalive.backend.dto.bot;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record InvestBotRequest(
        @NotNull @Positive BigDecimal amount,
        boolean autoReinvest,
        BigDecimal reinvestPercentage
) {
}
