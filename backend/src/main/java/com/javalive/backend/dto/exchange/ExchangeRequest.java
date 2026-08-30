package com.javalive.backend.dto.exchange;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Note: unlike the source app, there is deliberately no client-supplied {@code quantity} field —
 * the source trusted {@code $request->quantity} directly when crediting the destination currency,
 * letting a client fabricate any exchange amount regardless of the real rate. The server computes
 * the quantity itself from the live rate, the same way the quote endpoint does.
 */
public record ExchangeRequest(
        @NotBlank String source,
        @NotBlank String destination,
        @NotNull @DecimalMin(value = "0.00000001") BigDecimal amount
) {
}
