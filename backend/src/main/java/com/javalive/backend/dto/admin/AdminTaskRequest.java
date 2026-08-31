package com.javalive.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AdminTaskRequest(
        @NotBlank String title,
        String note,
        @NotNull Long assignedAdminId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotBlank String priority
) {
}
