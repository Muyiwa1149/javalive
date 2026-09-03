package com.javalive.backend.dto.admin;

public record AdminAppInfoRequest(
        String siteName, String siteTitle, String siteAddress, String description, String keywords,
        String timezone, String welcomeMessage, boolean googleTranslateEnabled, String tradeMode,
        Integer tradingWinrate, String merchantKey, String contactEmail, String defaultCurrencySymbol,
        boolean weekendTradeEnabled, boolean enableEmailVerification, boolean enableKyc,
        boolean enableKycRegistration, String captchaProvider, boolean socialLoginEnabled,
        boolean returnCapital, boolean shouldCancelPlan,
        String mailServer, String mailFromAddress, String mailFromName, String smtpHost, String smtpPort,
        String smtpEncryption, String smtpUsername, String smtpPassword, String googleClientId,
        String googleClientSecret, String googleRedirectUri, String captchaSecret, String captchaSiteKey
) {
}
