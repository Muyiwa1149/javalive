package com.javalive.backend.dto.plan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** {@code amount} is optional — defaults to the plan's base price when omitted, mirroring the source's `iamount` fallback. */
public record PurchasePlanRequest(
        @NotNull Long planId,
        @Positive BigDecimal amount,
        @NotBlank String duration
) {
}
