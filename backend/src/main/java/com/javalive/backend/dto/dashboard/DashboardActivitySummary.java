package com.javalive.backend.dto.dashboard;

import com.javalive.backend.entity.LedgerTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DashboardActivitySummary(
        Long id, String planLabel, BigDecimal amount, String type, LocalDateTime createdAt
) {
    public static DashboardActivitySummary from(LedgerTransaction t) {
        return new DashboardActivitySummary(t.getId(), t.getPlanLabel(), t.getAmount(), t.getType(), t.getCreatedAt());
    }
}
