package com.javalive.backend.dto.trade;

import java.util.List;

public record TradeMonitorDetail(
        UserTradeSummary trade, InstrumentSummary instrument, List<UserTradeSummary> relatedTrades,
        TradeStats stats, TradePnl pnl, String timeLeft
) {
}
