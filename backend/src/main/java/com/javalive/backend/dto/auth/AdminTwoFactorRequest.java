package com.javalive.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record AdminTwoFactorRequest(@NotBlank String code) {
}
