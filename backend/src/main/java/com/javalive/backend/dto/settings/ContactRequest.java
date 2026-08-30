package com.javalive.backend.dto.settings;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ContactRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String subject,
        @NotBlank String message
) {
}
