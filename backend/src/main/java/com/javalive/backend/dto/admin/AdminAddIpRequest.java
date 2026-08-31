package com.javalive.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record AdminAddIpRequest(@NotBlank String ipAddress) {
}
