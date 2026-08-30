package com.javalive.backend.dto.transfer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** {@code recipient} is matched against either email or username, mirroring the source's lookup. */
public record TransferRequest(
        @NotBlank String recipient,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String password
) {
}
