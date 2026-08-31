package com.javalive.backend.dto.wallet;

import com.javalive.backend.entity.Wallet;

import java.time.LocalDateTime;

/** Never includes the phrase — it stays encrypted server-side, decrypted only for an admin's explicit on-demand view (Phase 5). */
public record WalletStatus(boolean connected, String walletName, String status, LocalDateTime lastValidated) {
    public static WalletStatus from(Wallet w) {
        return new WalletStatus(true, w.getWalletName(), w.getStatus(), w.getLastValidated());
    }

    public static WalletStatus notConnected() {
        return new WalletStatus(false, null, null, null);
    }
}
