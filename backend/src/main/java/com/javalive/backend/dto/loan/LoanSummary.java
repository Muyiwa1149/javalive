package com.javalive.backend.dto.loan;

import com.javalive.backend.entity.Loan;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoanSummary(
        Long id, BigDecimal amount, String facility, String duration, String purpose, String income,
        String active, LocalDateTime createdAt
) {
    public static LoanSummary from(Loan l) {
        return new LoanSummary(l.getId(), l.getAmount(), l.getFacility(), l.getDuration(), l.getPurpose(),
                l.getIncome(), l.getActive(), l.getCreatedAt());
    }
}
