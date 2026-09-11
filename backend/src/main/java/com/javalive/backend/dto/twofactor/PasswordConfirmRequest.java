package com.javalive.backend.dto.twofactor;

import jakarta.validation.constraints.NotBlank;

/** Re-auth gate for disabling 2FA or regenerating recovery codes — matches Jetstream's
 *  {@code confirmPassword: true} setting for this feature. */
public record PasswordConfirmRequest(@NotBlank String password) {
}
