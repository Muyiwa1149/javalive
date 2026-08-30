package com.javalive.backend.dto.exchange;

import java.math.BigDecimal;

public record ExchangeQuoteResponse(BigDecimal quantity, BigDecimal fee, BigDecimal feePercentage, BigDecimal exchangeRate) {
}
