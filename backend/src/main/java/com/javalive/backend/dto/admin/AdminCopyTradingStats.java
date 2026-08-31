package com.javalive.backend.dto.admin;

import java.math.BigDecimal;

public record AdminCopyTradingStats(
        long totalExperts, long activeExperts, long totalCopyTrades, long activeCopyTrades,
        BigDecimal totalInvested, BigDecimal totalProfit, long totalUsersCopying
) {
}
