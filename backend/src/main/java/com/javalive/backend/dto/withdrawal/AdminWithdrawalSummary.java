package com.javalive.backend.dto.withdrawal;

import com.javalive.backend.entity.Withdrawal;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminWithdrawalSummary(
        Long id, Long userId, String userName, String userEmail, BigDecimal amount, BigDecimal toDeduct,
        String paymentMode, String status, String payDetails, LocalDateTime createdAt
) {
    public static AdminWithdrawalSummary from(Withdrawal w) {
        return new AdminWithdrawalSummary(
                w.getId(), w.getUser().getId(), w.getUser().getName(), w.getUser().getEmail(),
                w.getAmount(), w.getToDeduct(), w.getPaymentMode(), w.getStatus(), w.getPayDetails(), w.getCreatedAt()
        );
    }
}
