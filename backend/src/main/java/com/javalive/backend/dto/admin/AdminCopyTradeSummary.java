package com.javalive.backend.dto.admin;

import com.javalive.backend.entity.UserCopyTrade;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminCopyTradeSummary(
        Long id, String userName, String userEmail, String expertName, BigDecimal price, String active,
        BigDecimal totalProfit, BigDecimal currentBalance, Integer totalTrades, Integer winningTrades,
        LocalDateTime startedAt
) {
    public static AdminCopyTradeSummary from(UserCopyTrade t) {
        return new AdminCopyTradeSummary(t.getId(), t.getUser().getName(), t.getUser().getEmail(),
                t.getExpert().getName(), t.getPrice(), t.getActive(), t.getTotalProfit(), t.getCurrentBalance(),
                t.getTotalTrades(), t.getWinningTrades(), t.getStartedAt());
    }
}
