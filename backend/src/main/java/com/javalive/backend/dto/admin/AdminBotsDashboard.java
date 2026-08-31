package com.javalive.backend.dto.admin;

import java.math.BigDecimal;
import java.util.List;

public record AdminBotsDashboard(
        long totalBots, long activeBots, BigDecimal totalInvestments, BigDecimal totalProfits,
        long activeInvestments, long completedInvestments, List<AdminTopBot> topBots,
        List<AdminDailyProfit> dailyProfits
) {
    public record AdminTopBot(Long id, String name, long investmentsCount, long activeInvestmentsCount, BigDecimal totalProfits) {
    }

    public record AdminDailyProfit(String date, BigDecimal profit) {
    }
}
