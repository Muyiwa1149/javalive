package com.javalive.backend.dto.admin;

import com.javalive.backend.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminUserDetail(
        Long id, String name, String username, String email, String phone, String country, LocalDate dob,
        String status, String accountVerifyStatus, String tradeType, String tradeMode,
        BigDecimal accountBalance, BigDecimal roiBalance, BigDecimal bonusBalance, BigDecimal referralBonusBalance,
        String currencySymbol, String currencyCode, boolean emailVerified, LocalDateTime createdAt
) {
    public static AdminUserDetail from(User u) {
        return new AdminUserDetail(u.getId(), u.getName(), u.getUsername(), u.getEmail(), u.getPhone(),
                u.getCountry(), u.getDob(), u.getStatus(), u.getAccountVerifyStatus(), u.getTradeType(),
                u.getTradeMode(), u.getAccountBalance(), u.getRoiBalance(), u.getBonusBalance(),
                u.getReferralBonusBalance(), u.getCurrencySymbol(), u.getCurrencyCode(),
                u.getEmailVerifiedAt() != null, u.getCreatedAt());
    }
}
