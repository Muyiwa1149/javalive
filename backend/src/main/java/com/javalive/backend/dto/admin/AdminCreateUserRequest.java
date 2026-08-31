package com.javalive.backend.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCreateUserRequest(
        @NotBlank String name,
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password
) {
}
