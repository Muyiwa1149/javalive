package com.javalive.backend.dto.withdrawal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * {@code details} carries a crypto wallet address for crypto methods; the bank fields carry Bank
 * Transfer / Wire / ACH details. {@code otpCode} is only required when the user has OTP email
 * confirmation enabled.
 */
public record SubmitWithdrawalRequest(
        @NotNull Long methodId,
        @NotNull @Positive BigDecimal amount,
        String otpCode,
        String details,
        String bankName,
        String accountName,
        String accountNumber,
        String swiftCode
) {
}
