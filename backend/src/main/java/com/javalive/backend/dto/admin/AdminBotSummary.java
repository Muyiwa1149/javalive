package com.javalive.backend.dto.admin;

import com.javalive.backend.entity.TradingBot;

import java.math.BigDecimal;

public record AdminBotSummary(
        Long id, String name, String botType, String description, String image, BigDecimal minInvestment,
        BigDecimal maxInvestment, BigDecimal dailyProfitMin, BigDecimal dailyProfitMax, Integer successRate,
        Integer durationDays, String status, String tradingPairs, String riskSettings, String strategyDetails,
        long investmentsCount, long activeInvestmentsCount
) {
    public static AdminBotSummary from(TradingBot b, long investmentsCount, long activeInvestmentsCount) {
        return new AdminBotSummary(b.getId(), b.getName(), b.getBotType(), b.getDescription(), b.getImage(),
                b.getMinInvestment(), b.getMaxInvestment(), b.getDailyProfitMin(), b.getDailyProfitMax(),
                b.getSuccessRate(), b.getDurationDays(), b.getStatus(), b.getTradingPairs(), b.getRiskSettings(),
                b.getStrategyDetails(), investmentsCount, activeInvestmentsCount);
    }
}
