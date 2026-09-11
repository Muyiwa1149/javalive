package com.javalive.backend.dto.dashboard;

import com.javalive.backend.entity.Investment;

import java.math.BigDecimal;

/**
 * Was wrongly built against the legacy {@code UserPlan}/{@code user_plans} entity (used by the
 * Trade/Markets feature's placements, a separate concern) instead of {@code Investment} — the
 * entity {@code PlanService}/{@code MyPlans.vue} actually write to and display. That mismatch made
 * this dashboard preview card always show "No active plans yet" for any user with real investments.
 * Found via live testing while doing the Phase 7 mobile-optimization pass.
 */
public record DashboardPlanSummary(
        Long id, String planName, BigDecimal amount, BigDecimal profitEarned, String status
) {
    public static DashboardPlanSummary from(Investment investment) {
        BigDecimal earned = investment.getProfitEarned() == null ? BigDecimal.ZERO : investment.getProfitEarned();
        return new DashboardPlanSummary(
                investment.getId(), investment.getPlan() != null ? investment.getPlan().getName() : null,
                investment.getAmount(), earned, investment.getActive()
        );
    }
}
