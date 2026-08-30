package com.javalive.backend.dto.dashboard;

import com.javalive.backend.entity.UserPlan;

import java.math.BigDecimal;

public record DashboardPlanSummary(
        Long id, String planName, BigDecimal amount, BigDecimal profitEarned, String status
) {
    public static DashboardPlanSummary from(UserPlan up) {
        return new DashboardPlanSummary(
                up.getId(), up.getPlan() != null ? up.getPlan().getName() : up.getAssetSymbol(),
                up.getAmount(), up.getProfitEarned(), up.getStatus()
        );
    }
}
