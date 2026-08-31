package com.javalive.backend.dto.admin;

import java.math.BigDecimal;
import java.util.List;

public record AdminBotDetail(
        AdminBotSummary bot, long totalUsers, long activeInvestments, BigDecimal totalInvested,
        BigDecimal totalProfits, double avgSuccessRate, List<AdminBotTradeSummary> recentTrades
) {
}
