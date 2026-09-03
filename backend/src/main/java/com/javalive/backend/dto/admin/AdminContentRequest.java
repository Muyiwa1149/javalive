package com.javalive.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record AdminContentRequest(@NotBlank String title, @NotBlank String description) {
}
