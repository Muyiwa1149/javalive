package com.javalive.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record AdminUpdateProfileRequest(@NotBlank String firstName, @NotBlank String lastName, String phone, boolean enable2fa) {
}
