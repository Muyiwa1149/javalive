package com.javalive.backend.dto.user;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record UpdateProfileRequest(
        @NotBlank String name,
        LocalDate dob,
        String phone,
        String address
) {
}
