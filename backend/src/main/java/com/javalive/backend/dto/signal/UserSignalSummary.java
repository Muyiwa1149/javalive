package com.javalive.backend.dto.signal;

import com.javalive.backend.entity.UserSignalPlan;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserSignalSummary(
        Long id, String planName, String asset, String orderType, BigDecimal amount, String leverage,
        String status, String expiration, LocalDateTime createdAt
) {
    public static UserSignalSummary from(UserSignalPlan s) {
        return new UserSignalSummary(s.getId(), s.getSignalPlan() != null ? s.getSignalPlan().getName() : null,
                s.getAsset(), s.getOrderType(), s.getAmount(), s.getLeverage(), s.getStatus(), s.getExpiration(), s.getCreatedAt());
    }
}
