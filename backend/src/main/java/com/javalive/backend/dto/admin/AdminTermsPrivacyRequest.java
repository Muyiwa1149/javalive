package com.javalive.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record AdminTermsPrivacyRequest(@NotBlank String description, boolean useTerms) {
}
