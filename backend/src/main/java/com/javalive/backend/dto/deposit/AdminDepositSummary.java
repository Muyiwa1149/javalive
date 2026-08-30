package com.javalive.backend.dto.deposit;

import com.javalive.backend.entity.Deposit;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminDepositSummary(
        Long id, Long userId, String userName, String userEmail, BigDecimal amount, String paymentMode,
        String status, String proofImage, String txnId, LocalDateTime createdAt
) {
    public static AdminDepositSummary from(Deposit d) {
        return new AdminDepositSummary(
                d.getId(), d.getUser().getId(), d.getUser().getName(), d.getUser().getEmail(),
                d.getAmount(), d.getPaymentMode(), d.getStatus(), d.getProofImage(), d.getTxnId(), d.getCreatedAt()
        );
    }
}
