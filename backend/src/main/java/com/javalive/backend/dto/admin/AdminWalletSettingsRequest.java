package com.javalive.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AdminWalletSettingsRequest(@NotNull BigDecimal minBalance, @NotNull BigDecimal minReturn, @NotBlank String walletStatus) {
}
