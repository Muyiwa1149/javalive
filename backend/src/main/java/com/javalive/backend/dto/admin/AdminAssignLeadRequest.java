package com.javalive.backend.dto.admin;

import jakarta.validation.constraints.NotNull;

public record AdminAssignLeadRequest(@NotNull Long adminId) {
}
