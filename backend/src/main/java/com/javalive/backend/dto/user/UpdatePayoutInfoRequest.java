package com.javalive.backend.dto.user;

public record UpdatePayoutInfoRequest(
        String bankName, String bankAccountName, String bankAccountNumber, String bankSwiftCode,
        String btcAddress, String ethAddress, String ltcAddress, String usdtAddress
) {
}
