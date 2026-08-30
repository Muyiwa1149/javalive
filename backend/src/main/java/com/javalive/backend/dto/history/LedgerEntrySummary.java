package com.javalive.backend.dto.history;

import com.javalive.backend.entity.LedgerTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LedgerEntrySummary(Long id, String planLabel, BigDecimal amount, String type, LocalDateTime createdAt) {
    public static LedgerEntrySummary from(LedgerTransaction t) {
        return new LedgerEntrySummary(t.getId(), t.getPlanLabel(), t.getAmount(), t.getType(), t.getCreatedAt());
    }
}
