package com.javalive.backend.dto.bot;

import java.math.BigDecimal;
import java.util.List;

public record BotDashboard(
        List<BotInvestmentSummary> investments, long activeBots, BigDecimal totalInvested,
        BigDecimal currentBalance, BigDecimal totalProfit, BigDecimal totalLoss, List<BotTradeSummary> recentTrades
) {
}
