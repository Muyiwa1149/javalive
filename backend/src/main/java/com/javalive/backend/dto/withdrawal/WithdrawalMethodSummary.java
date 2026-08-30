package com.javalive.backend.dto.withdrawal;

import com.javalive.backend.entity.WithdrawalMethod;

import java.math.BigDecimal;

public record WithdrawalMethodSummary(
        Long id, String name, String methodType, BigDecimal minimumAmount, BigDecimal maximumAmount,
        BigDecimal chargesAmount, String chargesType, String durationNote, String imageUrl, String network
) {
    public static WithdrawalMethodSummary from(WithdrawalMethod m) {
        return new WithdrawalMethodSummary(
                m.getId(), m.getName(), m.getMethodType(), m.getMinimumAmount(), m.getMaximumAmount(),
                m.getChargesAmount(), m.getChargesType(), m.getDurationNote(), m.getImageUrl(), m.getNetwork()
        );
    }
}
