package com.javalive.backend.dto.auth;

/**
 * When {@code twoFactorRequired} is true, {@code token} is a short-lived pending-2FA token that
 * must be sent back to POST /api/auth/2fa/verify (not usable against any other endpoint) and
 * {@code user} is null — mirrors {@link AdminLoginResponse}'s same pattern on the admin side.
 */
public record UserLoginResponse(boolean twoFactorRequired, String token, UserSummary user) {
}
