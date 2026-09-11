package com.javalive.backend.dto.twofactor;

import jakarta.validation.constraints.NotBlank;

public record TwoFactorConfirmRequest(@NotBlank String code) {
}
