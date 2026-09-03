package com.javalive.backend.dto.admin;

import java.math.BigDecimal;

public record AdminReferralSettingsRequest(
        BigDecimal referralCommissionPct, BigDecimal referralCommissionL1, BigDecimal referralCommissionL2,
        BigDecimal referralCommissionL3, BigDecimal referralCommissionL4, BigDecimal referralCommissionL5,
        BigDecimal signupBonus, BigDecimal depositBonusPct
) {
}
