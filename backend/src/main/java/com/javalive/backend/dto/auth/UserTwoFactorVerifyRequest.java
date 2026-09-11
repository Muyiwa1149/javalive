package com.javalive.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;

/** {@code code} may be a 6-digit TOTP code or one of the user's recovery codes — both are accepted here,
 *  matching Jetstream's real login-challenge behavior. */
public record UserTwoFactorVerifyRequest(@NotBlank String code) {
}
