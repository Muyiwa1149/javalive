package com.javalive.backend.dto.twofactor;

import java.time.LocalDateTime;

public record TwoFactorStatusResponse(boolean enabled, LocalDateTime confirmedAt) {
}
