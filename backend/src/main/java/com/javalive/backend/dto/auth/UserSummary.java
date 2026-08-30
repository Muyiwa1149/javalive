package com.javalive.backend.dto.auth;

import com.javalive.backend.entity.User;

import java.math.BigDecimal;

/** Safe-to-expose projection of {@link User} — never serialize the entity itself (it carries the password hash). */
public record UserSummary(
        Long id,
        String name,
        String email,
        String username,
        String status,
        String accountVerifyStatus,
        BigDecimal accountBalance,
        BigDecimal roiBalance,
        BigDecimal bonusBalance,
        String currencySymbol
) {
    public static UserSummary from(User user) {
        return new UserSummary(
                user.getId(), user.getName(), user.getEmail(), user.getUsername(), user.getStatus(),
                user.getAccountVerifyStatus(), user.getAccountBalance(), user.getRoiBalance(),
                user.getBonusBalance(), user.getCurrencySymbol()
        );
    }
}
