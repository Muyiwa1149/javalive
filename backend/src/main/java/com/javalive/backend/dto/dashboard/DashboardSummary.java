package com.javalive.backend.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummary(
        BigDecimal accountBalance,
        BigDecimal roiBalance,
        BigDecimal bonusBalance,
        String currencySymbol,
        String accountVerifyStatus,
        boolean kycRequired,
        String referralLink,
        BigDecimal totalDeposited,
        BigDecimal totalWithdrawn,
        long tradingAccountsCount,
        List<DashboardPlanSummary> recentPlans,
        List<DashboardActivitySummary> recentActivity
) {
}
