package com.javalive.backend.dto.admin;

import java.math.BigDecimal;

public record AdminSubscriptionSettingsRequest(
        BigDecimal subscriptionMonthlyFee, BigDecimal subscriptionQuarterlyFee, BigDecimal subscriptionYearlyFee
) {
}
