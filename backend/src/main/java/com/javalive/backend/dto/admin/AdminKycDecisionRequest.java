package com.javalive.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record AdminKycDecisionRequest(@NotBlank String action, String subject, String message) {
}
