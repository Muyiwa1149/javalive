package com.javalive.backend.dto.copytrading;

import com.javalive.backend.entity.CopyTradingExpert;

import java.math.BigDecimal;

public record ExpertSummary(
        Long id, String name, String tag, String photo, Integer rating, Integer followers,
        BigDecimal equity, BigDecimal totalProfit, Integer winRate, Integer totalTrades,
        BigDecimal price, String type, String description, boolean alreadyCopying
) {
    public static ExpertSummary from(CopyTradingExpert e, boolean alreadyCopying) {
        return new ExpertSummary(
                e.getId(), e.getName(), e.getTag(), e.getPhoto(), e.getRating(), e.getFollowers(),
                e.getEquity(), e.getTotalProfit(), e.getWinRate(), e.getTotalTrades(),
                e.getPrice(), e.getType(), e.getDescription(), alreadyCopying
        );
    }
}
