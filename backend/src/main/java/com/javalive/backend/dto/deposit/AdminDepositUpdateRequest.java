package com.javalive.backend.dto.deposit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Full admin correction of a deposit record — amount, date/time, status, method, and reference id.
 *
 * @param adjustBalance when true, the user's account balance is credited (or debited, if the
 *                       amount was lowered) by the difference between the old and new amount —
 *                       an explicit opt-in since the raw edit otherwise never touches the balance.
 */
public record AdminDepositUpdateRequest(
        @NotNull BigDecimal amount,
        String paymentMode,
        @NotBlank String status,
        String txnId,
        LocalDateTime createdAt,
        boolean adjustBalance
) {
}
