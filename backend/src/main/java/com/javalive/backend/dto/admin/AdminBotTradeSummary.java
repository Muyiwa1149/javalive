package com.javalive.backend.dto.admin;

import com.javalive.backend.entity.BotTradingHistory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminBotTradeSummary(
        Long id, String userName, String tradeType, String tradingPair, BigDecimal entryPrice, BigDecimal exitPrice,
        BigDecimal amount, BigDecimal profitLoss, String result, LocalDateTime openedAt, LocalDateTime closedAt
) {
    public static AdminBotTradeSummary from(BotTradingHistory t) {
        String userName = t.getUserBotInvestment() != null && t.getUserBotInvestment().getUser() != null
                ? t.getUserBotInvestment().getUser().getName() : null;
        return new AdminBotTradeSummary(t.getId(), userName, t.getTradeType(), t.getTradingPair(), t.getEntryPrice(),
                t.getExitPrice(), t.getAmount(), t.getProfitLoss(), t.getResult(), t.getOpenedAt(), t.getClosedAt());
    }
}
