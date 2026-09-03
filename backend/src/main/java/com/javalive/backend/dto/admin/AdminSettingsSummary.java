package com.javalive.backend.dto.admin;

import com.javalive.backend.entity.AppSetting;

import java.math.BigDecimal;

/** Full admin-editable settings projection (Super Admin only) — mirrors every field the source's 5 Settings pages read/write. */
public record AdminSettingsSummary(
        // App info
        String siteName, String siteTitle, String siteAddress, String description, String keywords, String logo,
        String favicon, String timezone, String welcomeMessage, boolean googleTranslateEnabled, String tradeMode,
        Integer tradingWinrate, String merchantKey,
        // Preferences
        String contactEmail, String defaultCurrencySymbol, boolean weekendTradeEnabled, boolean enableEmailVerification,
        boolean enableKyc, boolean enableKycRegistration, String captchaProvider, boolean socialLoginEnabled,
        boolean returnCapital, boolean shouldCancelPlan,
        // Email/SMTP
        String mailServer, String mailFromAddress, String mailFromName, String smtpHost, String smtpPort,
        String smtpEncryption, String smtpUsername, String smtpPassword, String googleClientId, String googleClientSecret,
        String googleRedirectUri, String captchaSecret, String captchaSiteKey,
        // Referral / bonus
        BigDecimal referralCommissionPct, BigDecimal referralCommissionL1, BigDecimal referralCommissionL2,
        BigDecimal referralCommissionL3, BigDecimal referralCommissionL4, BigDecimal referralCommissionL5,
        BigDecimal signupBonus, BigDecimal depositBonusPct,
        // Subscription fees
        BigDecimal subscriptionMonthlyFee, BigDecimal subscriptionQuarterlyFee, BigDecimal subscriptionYearlyFee,
        // Payment gateways
        String withdrawalOption, String depositOption, String autoMerchantOption, String deductionOption,
        String creditCardProvider, BigDecimal minTopupAmount, String stripeSecretKey, String stripePublicKey,
        String paypalClientId, String paypalClientSecret, String paystackPublicKey, String paystackSecretKey,
        String paystackUrl, String paystackEmail, String flutterwavePublicKey, String flutterwaveSecretKey,
        String flutterwaveSecretHash, String binanceApiKey, String binanceSecretKey, String coinpaymentsPublicKey,
        String coinpaymentsPrivateKey, String coinpaymentsMerchantId, String coinpaymentsIpnSecret,
        String coinpaymentsDebugEmail,
        // Transfer
        boolean useInternalTransfer, BigDecimal minTransferAmount, BigDecimal transferCharges,
        // Crypto / exchange
        boolean useCryptoFeature, BigDecimal exchangeFeePct, BigDecimal currencyRate, String localCurrency,
        String baseCurrency
) {
    public static AdminSettingsSummary from(AppSetting s) {
        return new AdminSettingsSummary(
                s.getSiteName(), s.getSiteTitle(), s.getSiteAddress(), s.getDescription(), s.getKeywords(), s.getLogo(),
                s.getFavicon(), s.getTimezone(), s.getWelcomeMessage(), bool(s.getGoogleTranslateEnabled()), s.getTradeMode(),
                s.getTradingWinrate(), s.getMerchantKey(),
                s.getContactEmail(), s.getDefaultCurrencySymbol(), bool(s.getWeekendTradeEnabled()), bool(s.getEnableEmailVerification()),
                bool(s.getEnableKyc()), bool(s.getEnableKycRegistration()), s.getCaptchaProvider(), bool(s.getSocialLoginEnabled()),
                bool(s.getReturnCapital()), bool(s.getShouldCancelPlan()),
                s.getMailServer(), s.getMailFromAddress(), s.getMailFromName(), s.getSmtpHost(), s.getSmtpPort(),
                s.getSmtpEncryption(), s.getSmtpUsername(), s.getSmtpPassword(), s.getGoogleClientId(), s.getGoogleClientSecret(),
                s.getGoogleRedirectUri(), s.getCaptchaSecret(), s.getCaptchaSiteKey(),
                s.getReferralCommissionPct(), s.getReferralCommissionL1(), s.getReferralCommissionL2(),
                s.getReferralCommissionL3(), s.getReferralCommissionL4(), s.getReferralCommissionL5(),
                s.getSignupBonus(), s.getDepositBonusPct(),
                s.getSubscriptionMonthlyFee(), s.getSubscriptionQuarterlyFee(), s.getSubscriptionYearlyFee(),
                s.getWithdrawalOption(), s.getDepositOption(), s.getAutoMerchantOption(), s.getDeductionOption(),
                s.getCreditCardProvider(), s.getMinTopupAmount(), s.getStripeSecretKey(), s.getStripePublicKey(),
                s.getPaypalClientId(), s.getPaypalClientSecret(), s.getPaystackPublicKey(), s.getPaystackSecretKey(),
                s.getPaystackUrl(), s.getPaystackEmail(), s.getFlutterwavePublicKey(), s.getFlutterwaveSecretKey(),
                s.getFlutterwaveSecretHash(), s.getBinanceApiKey(), s.getBinanceSecretKey(), s.getCoinpaymentsPublicKey(),
                s.getCoinpaymentsPrivateKey(), s.getCoinpaymentsMerchantId(), s.getCoinpaymentsIpnSecret(),
                s.getCoinpaymentsDebugEmail(),
                bool(s.getUseInternalTransfer()), s.getMinTransferAmount(), s.getTransferCharges(),
                bool(s.getUseCryptoFeature()), s.getExchangeFeePct(), s.getCurrencyRate(), s.getLocalCurrency(),
                s.getBaseCurrency()
        );
    }

    private static boolean bool(Boolean b) {
        return Boolean.TRUE.equals(b);
    }
}
