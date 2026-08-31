package com.javalive.backend.dto.mt4;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record Mt4SubscriptionRequest(
        @NotBlank String userid,
        @NotBlank String pswrd,
        @NotBlank String acntype,
        @NotBlank String name,
        @NotBlank String currency,
        @NotBlank String leverage,
        @NotBlank String server,
        @NotBlank String duration,
        @NotNull @PositiveOrZero BigDecimal amount
) {
}
