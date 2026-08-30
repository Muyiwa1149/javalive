package com.javalive.backend.dto.plan;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record WithdrawProfitRequest(@NotNull @Positive BigDecimal amount) {
}
