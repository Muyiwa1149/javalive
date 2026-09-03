package com.javalive.backend.dto.admin;

import com.javalive.backend.entity.WithdrawalMethod;

import java.math.BigDecimal;

public record AdminWithdrawalMethodSummary(
        Long id, String name, String methodType, String type, BigDecimal minimumAmount, BigDecimal maximumAmount,
        BigDecimal chargesAmount, String chargesType, String durationNote, String imageUrl, String bankName,
        String accountName, String accountNumber, String swiftCode, String walletAddress, String barcodeImage,
        String network, boolean isDefault, String status
) {
    public static AdminWithdrawalMethodSummary from(WithdrawalMethod m) {
        return new AdminWithdrawalMethodSummary(m.getId(), m.getName(), m.getMethodType(), m.getType(),
                m.getMinimumAmount(), m.getMaximumAmount(), m.getChargesAmount(), m.getChargesType(),
                m.getDurationNote(), m.getImageUrl(), m.getBankName(), m.getAccountName(), m.getAccountNumber(),
                m.getSwiftCode(), m.getWalletAddress(), m.getBarcodeImage(), m.getNetwork(),
                Boolean.TRUE.equals(m.getIsDefault()), m.getStatus());
    }
}
