package com.javalive.backend.dto.bot;

import com.javalive.backend.entity.UserBotInvestment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BotInvestmentSummary(
        Long id, Long botId, String botName, String botImage, BigDecimal investmentAmount, BigDecimal currentBalance,
        BigDecimal totalProfit, BigDecimal totalLoss, Integer successfulTrades, Integer failedTrades,
        String status, LocalDateTime startedAt, LocalDateTime expiresAt
) {
    public static BotInvestmentSummary from(UserBotInvestment i) {
        return new BotInvestmentSummary(
                i.getId(), i.getBot().getId(), i.getBot().getName(), i.getBot().getImage(), i.getInvestmentAmount(),
                i.getCurrentBalance(), i.getTotalProfit(), i.getTotalLoss(), i.getSuccessfulTrades(),
                i.getFailedTrades(), i.getStatus(), i.getStartedAt(), i.getExpiresAt()
        );
    }
}
