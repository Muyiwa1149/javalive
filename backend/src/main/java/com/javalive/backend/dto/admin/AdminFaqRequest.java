package com.javalive.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record AdminFaqRequest(@NotBlank String question, @NotBlank String answer) {
}
