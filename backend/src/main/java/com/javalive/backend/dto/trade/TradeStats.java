package com.javalive.backend.dto.trade;

import java.math.BigDecimal;

public record TradeStats(long totalTrades, long completedTrades, long activeTrades, BigDecimal totalInvested, BigDecimal avgTradeSize) {
}
