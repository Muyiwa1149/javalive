package com.javalive.backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserPasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 8) String password
) {
}
