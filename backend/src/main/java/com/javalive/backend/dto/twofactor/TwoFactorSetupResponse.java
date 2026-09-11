package com.javalive.backend.dto.twofactor;

/** {@code qrCodeDataUri} is a ready-to-render {@code data:image/png;base64,...} string — no frontend
 *  QR library needed. {@code secret} is shown too, for manual entry when scanning isn't convenient. */
public record TwoFactorSetupResponse(String secret, String otpauthUri, String qrCodeDataUri) {
}
