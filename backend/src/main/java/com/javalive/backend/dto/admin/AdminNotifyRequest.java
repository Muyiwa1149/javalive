package com.javalive.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record AdminNotifyRequest(@NotBlank String message) {
}
