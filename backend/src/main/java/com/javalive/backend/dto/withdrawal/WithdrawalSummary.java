package com.javalive.backend.dto.withdrawal;

import com.javalive.backend.entity.Withdrawal;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WithdrawalSummary(
        Long id, BigDecimal amount, BigDecimal toDeduct, String paymentMode, String status,
        String payDetails, LocalDateTime createdAt
) {
    public static WithdrawalSummary from(Withdrawal w) {
        return new WithdrawalSummary(w.getId(), w.getAmount(), w.getToDeduct(), w.getPaymentMode(),
                w.getStatus(), w.getPayDetails(), w.getCreatedAt());
    }
}
