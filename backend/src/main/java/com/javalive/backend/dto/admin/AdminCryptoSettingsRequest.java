package com.javalive.backend.dto.admin;

import java.math.BigDecimal;

public record AdminCryptoSettingsRequest(
        boolean useCryptoFeature, BigDecimal exchangeFeePct, BigDecimal currencyRate, String localCurrency,
        String baseCurrency
) {
}
