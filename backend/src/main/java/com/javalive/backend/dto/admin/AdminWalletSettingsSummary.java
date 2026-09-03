package com.javalive.backend.dto.admin;

import java.math.BigDecimal;

public record AdminWalletSettingsSummary(BigDecimal minBalance, BigDecimal minReturn, String walletStatus) {
}
