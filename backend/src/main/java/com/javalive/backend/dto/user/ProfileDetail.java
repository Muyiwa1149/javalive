package com.javalive.backend.dto.user;

import com.javalive.backend.entity.User;

import java.time.LocalDate;

public record ProfileDetail(
        Long id, String name, String username, String email, String phone, String country,
        LocalDate dob, String address,
        String bankName, String bankAccountName, String bankAccountNumber, String bankSwiftCode,
        String btcAddress, String ethAddress, String ltcAddress, String usdtAddress,
        boolean sendOtpEmail, boolean sendRoiEmail, boolean sendInvPlanEmail,
        String accountVerifyStatus
) {
    public static ProfileDetail from(User u) {
        return new ProfileDetail(
                u.getId(), u.getName(), u.getUsername(), u.getEmail(), u.getPhone(), u.getCountry(),
                u.getDob(), u.getAddress(),
                u.getBankName(), u.getBankAccountName(), u.getBankAccountNumber(), u.getBankSwiftCode(),
                u.getBtcAddress(), u.getEthAddress(), u.getLtcAddress(), u.getUsdtAddress(),
                Boolean.TRUE.equals(u.getSendOtpEmail()), Boolean.TRUE.equals(u.getSendRoiEmail()),
                Boolean.TRUE.equals(u.getSendInvPlanEmail()), u.getAccountVerifyStatus()
        );
    }
}
