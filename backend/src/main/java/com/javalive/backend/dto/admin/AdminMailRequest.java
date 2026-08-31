package com.javalive.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record AdminMailRequest(@NotBlank String subject, @NotBlank String message) {
}
