package com.javalive.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminUpdatePasswordRequest(
        @NotBlank String oldPassword,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String passwordConfirmation
) {
}
