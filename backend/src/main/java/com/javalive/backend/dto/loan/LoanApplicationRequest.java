package com.javalive.backend.dto.loan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record LoanApplicationRequest(
        @NotNull @Positive BigDecimal amount,
        @NotBlank String income,
        @NotBlank String purpose,
        @NotBlank String duration,
        @NotBlank String facility
) {
}
