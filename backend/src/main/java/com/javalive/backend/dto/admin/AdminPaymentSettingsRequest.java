package com.javalive.backend.dto.admin;

import java.math.BigDecimal;

public record AdminPaymentSettingsRequest(
        String withdrawalOption, String depositOption, String autoMerchantOption, String deductionOption,
        String creditCardProvider, BigDecimal minTopupAmount, boolean useInternalTransfer,
        BigDecimal minTransferAmount, BigDecimal transferCharges,
        String stripeSecretKey, String stripePublicKey, String paypalClientId, String paypalClientSecret,
        String paystackPublicKey, String paystackSecretKey, String paystackUrl, String paystackEmail,
        String flutterwavePublicKey, String flutterwaveSecretKey, String flutterwaveSecretHash,
        String binanceApiKey, String binanceSecretKey,
        String coinpaymentsPublicKey, String coinpaymentsPrivateKey, String coinpaymentsMerchantId,
        String coinpaymentsIpnSecret, String coinpaymentsDebugEmail
) {
}
