package com.javalive.backend.dto.bot;

import com.javalive.backend.entity.BotTradingHistory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BotTradeSummary(
        Long id, String tradeType, String tradingPair, BigDecimal entryPrice, BigDecimal exitPrice,
        BigDecimal amount, BigDecimal profitLoss, BigDecimal profitPercentage, String result,
        LocalDateTime openedAt, LocalDateTime closedAt
) {
    public static BotTradeSummary from(BotTradingHistory h) {
        return new BotTradeSummary(
                h.getId(), h.getTradeType(), h.getTradingPair(), h.getEntryPrice(), h.getExitPrice(),
                h.getAmount(), h.getProfitLoss(), h.getProfitPercentage(), h.getResult(), h.getOpenedAt(), h.getClosedAt()
        );
    }
}
