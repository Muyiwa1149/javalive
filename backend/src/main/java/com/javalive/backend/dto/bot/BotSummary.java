package com.javalive.backend.dto.bot;

import com.javalive.backend.entity.TradingBot;

import java.math.BigDecimal;

public record BotSummary(
        Long id, String name, String botType, String description, String image, BigDecimal minInvestment,
        BigDecimal maxInvestment, BigDecimal dailyProfitMin, BigDecimal dailyProfitMax, Integer successRate,
        Integer durationDays, Integer totalUsers, String status, boolean alreadyInvested
) {
    public static BotSummary from(TradingBot b, boolean alreadyInvested) {
        return new BotSummary(
                b.getId(), b.getName(), b.getBotType(), b.getDescription(), b.getImage(), b.getMinInvestment(),
                b.getMaxInvestment(), b.getDailyProfitMin(), b.getDailyProfitMax(), b.getSuccessRate(),
                b.getDurationDays(), b.getTotalUsers(), b.getStatus(), alreadyInvested
        );
    }
}
