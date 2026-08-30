package com.javalive.backend.dto.copytrading;

import java.math.BigDecimal;
import java.util.List;

public record CopyTradingDashboard(
        List<CopyTradeSummary> copyTrades, long activeCopies, BigDecimal totalInvested, BigDecimal currentBalance,
        BigDecimal totalProfit
) {
}
