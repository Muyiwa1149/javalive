package com.javalive.backend.dto.plan;

import com.javalive.backend.entity.Investment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvestmentSummary(
        Long id, Long planId, String planName, BigDecimal amount, String active, String invDuration,
        LocalDateTime expireDate, LocalDateTime activatedAt, BigDecimal profitEarned, BigDecimal profitWithdrawn,
        BigDecimal availableProfit, boolean withdrawalDisabled, BigDecimal planIncrementAmount
) {
    public static InvestmentSummary from(Investment i) {
        BigDecimal earned = i.getProfitEarned() == null ? BigDecimal.ZERO : i.getProfitEarned();
        BigDecimal withdrawn = i.getProfitWithdrawn() == null ? BigDecimal.ZERO : i.getProfitWithdrawn();
        return new InvestmentSummary(
                i.getId(), i.getPlan() != null ? i.getPlan().getId() : null,
                i.getPlan() != null ? i.getPlan().getName() : null, i.getAmount(), i.getActive(), i.getInvDuration(),
                i.getExpireDate(), i.getActivatedAt(), earned, withdrawn, earned.subtract(withdrawn),
                Boolean.TRUE.equals(i.getWithdrawalDisabled()),
                i.getPlan() != null ? i.getPlan().getIncrementAmount() : null
        );
    }
}
