package com.javalive.backend.dto.copytrading;

import com.javalive.backend.entity.UserCopyTrade;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CopyTradeSummary(
        Long id, Long expertId, String expertName, String expertPhoto, BigDecimal price, String active,
        LocalDateTime startedAt, BigDecimal totalProfit, BigDecimal currentBalance, Integer totalTrades,
        Integer winningTrades, BigDecimal profitPercentage
) {
    public static CopyTradeSummary from(UserCopyTrade c) {
        return new CopyTradeSummary(
                c.getId(), c.getExpert().getId(), c.getExpert().getName(), c.getExpert().getPhoto(),
                c.getPrice(), c.getActive(), c.getStartedAt(), c.getTotalProfit(), c.getCurrentBalance(),
                c.getTotalTrades(), c.getWinningTrades(), c.getProfitPercentage()
        );
    }
}
