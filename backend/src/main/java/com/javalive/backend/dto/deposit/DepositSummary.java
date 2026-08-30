package com.javalive.backend.dto.deposit;

import com.javalive.backend.entity.Deposit;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DepositSummary(
        Long id, BigDecimal amount, String paymentMode, String status, String proofImage,
        String txnId, LocalDateTime createdAt
) {
    public static DepositSummary from(Deposit d) {
        return new DepositSummary(d.getId(), d.getAmount(), d.getPaymentMode(), d.getStatus(),
                d.getProofImage(), d.getTxnId(), d.getCreatedAt());
    }
}
