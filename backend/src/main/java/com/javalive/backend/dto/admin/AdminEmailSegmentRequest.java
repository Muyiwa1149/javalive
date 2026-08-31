package com.javalive.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record AdminEmailSegmentRequest(
        @NotBlank String category,
        List<Long> userIds,
        @NotBlank String subject,
        @NotBlank String message
) {
}
