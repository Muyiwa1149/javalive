package com.javalive.backend.dto.trade;

import java.math.BigDecimal;

public record TradePnl(BigDecimal currentValue, BigDecimal profitLoss, BigDecimal returnPercentage, boolean isProfit, String status) {
}
