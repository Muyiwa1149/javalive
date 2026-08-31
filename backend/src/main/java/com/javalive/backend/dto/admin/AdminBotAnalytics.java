package com.javalive.backend.dto.admin;

import java.util.List;

public record AdminBotAnalytics(
        List<AdminBotsDashboard.AdminDailyProfit> dailyProfits, long totalTrades, long successfulTrades
) {
}
