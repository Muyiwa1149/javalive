package com.javalive.backend.dto.deposit;

import com.javalive.backend.entity.WithdrawalMethod;

import java.math.BigDecimal;

public record DepositMethodSummary(
        Long id, String name, String methodType, BigDecimal minimumAmount, BigDecimal maximumAmount,
        String durationNote, String imageUrl, String bankName, String accountName, String accountNumber,
        String swiftCode, String walletAddress, String barcodeImage, String network
) {
    public static DepositMethodSummary from(WithdrawalMethod m) {
        return new DepositMethodSummary(
                m.getId(), m.getName(), m.getMethodType(), m.getMinimumAmount(), m.getMaximumAmount(),
                m.getDurationNote(), m.getImageUrl(), m.getBankName(), m.getAccountName(), m.getAccountNumber(),
                m.getSwiftCode(), m.getWalletAddress(), m.getBarcodeImage(), m.getNetwork()
        );
    }
}
