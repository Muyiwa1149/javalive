package com.javalive.backend.dto.exchange;

import java.math.BigDecimal;

public record AssetBalance(String currency, BigDecimal balance, BigDecimal usdValue) {
}
