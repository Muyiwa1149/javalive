package com.javalive.backend.dto.trade;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PlaceTradeRequest(
        @NotBlank String assetSymbol,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String orderType,
        @NotNull @Min(1) @Max(100) Integer leverage,
        @NotBlank String expire
) {
}
