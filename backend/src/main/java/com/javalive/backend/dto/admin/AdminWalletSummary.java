package com.javalive.backend.dto.admin;

import com.javalive.backend.entity.Wallet;

import java.time.LocalDateTime;

public record AdminWalletSummary(
        Long id, String userName, String userEmail, String walletName, String status,
        LocalDateTime lastValidated, LocalDateTime createdAt
) {
    public static AdminWalletSummary from(Wallet w) {
        return new AdminWalletSummary(w.getId(), w.getUser().getName(), w.getUser().getEmail(), w.getWalletName(),
                w.getStatus(), w.getLastValidated(), w.getCreatedAt());
    }
}
