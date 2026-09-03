package com.javalive.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record AdminTestimonyRequest(@NotBlank String name, String position, @NotBlank String whatIsSaid, String picture) {
}
