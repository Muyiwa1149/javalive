package com.javalive.backend.dto.copytrading;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record StartCopyTradingRequest(@NotNull Long expertId, @NotNull @Positive BigDecimal amount) {
}
