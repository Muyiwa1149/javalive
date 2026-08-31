package com.javalive.backend.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AdminUserEditRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        String country,
        @NotBlank String username,
        String phone,
        String currencySymbol,
        String currencyCode
) {
}
