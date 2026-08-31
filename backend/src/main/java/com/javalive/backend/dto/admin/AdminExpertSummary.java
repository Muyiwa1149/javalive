package com.javalive.backend.dto.admin;

import com.javalive.backend.entity.CopyTradingExpert;

import java.math.BigDecimal;

public record AdminExpertSummary(
        Long id, String name, String tag, String photo, Integer rating, Integer followers, BigDecimal equity,
        BigDecimal totalProfit, String status, String description, Integer winRate, Integer totalTrades,
        BigDecimal price, long copiersCount, long activeCopiersCount
) {
    public static AdminExpertSummary from(CopyTradingExpert e, long copiersCount, long activeCopiersCount) {
        return new AdminExpertSummary(e.getId(), e.getName(), e.getTag(), e.getPhoto(), e.getRating(), e.getFollowers(),
                e.getEquity(), e.getTotalProfit(), e.getStatus(), e.getDescription(), e.getWinRate(),
                e.getTotalTrades(), e.getPrice(), copiersCount, activeCopiersCount);
    }
}
