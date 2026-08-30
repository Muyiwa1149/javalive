package com.javalive.backend.dto.exchange;

import com.javalive.backend.entity.CryptoRecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExchangeRecordSummary(Long id, String source, String dest, BigDecimal amount, BigDecimal quantity, LocalDateTime createdAt) {
    public static ExchangeRecordSummary from(CryptoRecord r) {
        return new ExchangeRecordSummary(r.getId(), r.getSource(), r.getDest(), r.getAmount(), r.getQuantity(), r.getCreatedAt());
    }
}
