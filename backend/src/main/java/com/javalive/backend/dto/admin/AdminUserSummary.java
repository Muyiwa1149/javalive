package com.javalive.backend.dto.admin;

import com.javalive.backend.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminUserSummary(
        Long id, String name, String username, String email, String phone, String country,
        String status, String accountVerifyStatus, BigDecimal accountBalance, String currencySymbol,
        LocalDateTime createdAt
) {
    public static AdminUserSummary from(User u) {
        return new AdminUserSummary(u.getId(), u.getName(), u.getUsername(), u.getEmail(), u.getPhone(),
                u.getCountry(), u.getStatus(), u.getAccountVerifyStatus(), u.getAccountBalance(),
                u.getCurrencySymbol(), u.getCreatedAt());
    }
}
