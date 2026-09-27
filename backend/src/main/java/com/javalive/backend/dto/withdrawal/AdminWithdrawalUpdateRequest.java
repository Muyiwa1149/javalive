package com.javalive.backend.dto.withdrawal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Full admin correction of a withdrawal record — amount, deduction, date/time, status, method, and
 * payout details.
 *
 * @param adjustBalance when true, the user's account balance is adjusted by the difference between
 *                       the old and new {@code toDeduct} (a lower deduction refunds the difference,
 *                       a higher one deducts more) — an explicit opt-in since the raw edit otherwise
 *                       never touches the balance.
 */
public record AdminWithdrawalUpdateRequest(
        @NotNull BigDecimal amount,
        BigDecimal toDeduct,
        String paymentMode,
        @NotBlank String status,
        String payDetails,
        LocalDateTime createdAt,
        boolean adjustBalance
) {
}
