package com.javalive.backend.dto.admin;

import com.javalive.backend.entity.Investment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminInvestmentSummary(
        Long id, String userName, String userEmail, String planName, BigDecimal amount, String active,
        String invDuration, LocalDateTime expireDate, LocalDateTime activatedAt, BigDecimal profitEarned,
        BigDecimal profitWithdrawn
) {
    public static AdminInvestmentSummary from(Investment i) {
        return new AdminInvestmentSummary(i.getId(), i.getUser().getName(), i.getUser().getEmail(),
                i.getPlan() != null ? i.getPlan().getName() : null, i.getAmount(), i.getActive(), i.getInvDuration(),
                i.getExpireDate(), i.getActivatedAt(), i.getProfitEarned(), i.getProfitWithdrawn());
    }
}
