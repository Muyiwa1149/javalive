package com.javalive.backend.service.settings;

import com.javalive.backend.entity.User;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Mirrors the source app's `EnsureKycIsCompleted` middleware, applied per-route rather than
 * globally (KYC is only required for money-moving actions, not e.g. viewing the dashboard) — Phase
 * 4 controllers call {@link #requireVerified} at the top of the relevant actions (deposit,
 * withdraw, plan purchase, etc.), gated by whether KYC is even turned on in {@link SettingsService}.
 */
@Service
public class KycGuardService {

    private final SettingsService settingsService;

    public KycGuardService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public void requireVerified(User user) {
        boolean kycRequired = Boolean.TRUE.equals(settingsService.get().getEnableKyc());
        if (!kycRequired) {
            return;
        }
        if (!"Verified".equalsIgnoreCase(user.getAccountVerifyStatus())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Please complete identity verification (KYC) before continuing.");
        }
    }
}
