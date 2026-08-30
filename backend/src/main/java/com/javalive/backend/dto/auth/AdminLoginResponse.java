package com.javalive.backend.dto.auth;

/**
 * When {@code twoFactorRequired} is true, {@code token} is a short-lived pending-2FA token that
 * must be sent back to POST /api/admin/auth/2fa (not usable against any other endpoint) and
 * {@code admin} is null — the client only learns who logged in once 2FA succeeds.
 */
public record AdminLoginResponse(boolean twoFactorRequired, String token, AdminSummary admin) {
}
