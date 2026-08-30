package com.javalive.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record ConfirmPasswordRequest(@NotBlank String password) {
}
