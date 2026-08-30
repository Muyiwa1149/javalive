package com.javalive.backend.dto.signal;

import com.javalive.backend.entity.SignalPlan;

import java.math.BigDecimal;

public record SignalPlanSummary(Long id, String name, BigDecimal price, String type, BigDecimal incrementAmount) {
    public static SignalPlanSummary from(SignalPlan p) {
        return new SignalPlanSummary(p.getId(), p.getName(), p.getPrice(), p.getType(), p.getIncrementAmount());
    }
}
