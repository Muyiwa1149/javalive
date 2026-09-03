package com.javalive.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record AdminWithdrawalMethodRequest(
        @NotBlank String name, String methodType, @NotBlank String type, BigDecimal minimumAmount,
        BigDecimal maximumAmount, BigDecimal chargesAmount, String chargesType, String durationNote,
        String imageUrl, String bankName, String accountName, String accountNumber, String swiftCode,
        String walletAddress, String network, @NotBlank String status
) {
}
