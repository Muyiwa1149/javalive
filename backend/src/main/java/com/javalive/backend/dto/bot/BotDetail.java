package com.javalive.backend.dto.bot;

import java.math.BigDecimal;
import java.util.List;

public record BotDetail(
        BotSummary bot, BotInvestmentSummary myInvestment, List<BotTradeSummary> recentTrades,
        BigDecimal expectedDailyReturn, long totalTradesAcrossAllUsers, long totalInvestors
) {
}
