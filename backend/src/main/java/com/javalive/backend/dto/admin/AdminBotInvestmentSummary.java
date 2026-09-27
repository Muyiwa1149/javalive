package com.javalive.backend.dto.admin;

import com.javalive.backend.entity.UserBotInvestment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Full row-level history of a bot investment — every status, not just currently-active ones. */
public record AdminBotInvestmentSummary(
        Long id, Long userId, String userName, String userEmail, Long botId, String botName,
        BigDecimal investmentAmount, BigDecimal currentBalance, BigDecimal totalProfit, BigDecimal totalLoss,
        Integer successfulTrades, Integer failedTrades, String status,
        LocalDateTime startedAt, LocalDateTime expiresAt, LocalDateTime createdAt
) {
    public static AdminBotInvestmentSummary from(UserBotInvestment i) {
        return new AdminBotInvestmentSummary(
                i.getId(), i.getUser().getId(), i.getUser().getName(), i.getUser().getEmail(),
                i.getBot().getId(), i.getBot().getName(),
                i.getInvestmentAmount(), i.getCurrentBalance(), i.getTotalProfit(), i.getTotalLoss(),
                i.getSuccessfulTrades(), i.getFailedTrades(), i.getStatus(),
                i.getStartedAt(), i.getExpiresAt(), i.getCreatedAt()
        );
    }
}
