package com.javalive.backend.dto.settings;

import com.javalive.backend.entity.AppSetting;

/**
 * Only the fields safe to expose to an unauthenticated client (branding, feature toggles) —
 * never include gateway secret keys, SMTP credentials, or the external-SaaS merchant key here.
 */
public record PublicSettingsSummary(
        String siteName,
        String siteTitle,
        String description,
        String logo,
        String favicon,
        String defaultCurrencySymbol,
        String contactEmail,
        String whatsappNumber,
        boolean socialLoginEnabled,
        boolean enableKyc,
        boolean enableKycRegistration,
        boolean enable2fa,
        boolean googleTranslateEnabled,
        boolean useCryptoFeature,
        String tawkToEmbed
) {
    public static PublicSettingsSummary from(AppSetting s) {
        return new PublicSettingsSummary(
                s.getSiteName(), s.getSiteTitle(), s.getDescription(), s.getLogo(), s.getFavicon(),
                s.getDefaultCurrencySymbol(), s.getContactEmail(), s.getWhatsappNumber(),
                Boolean.TRUE.equals(s.getSocialLoginEnabled()), Boolean.TRUE.equals(s.getEnableKyc()),
                Boolean.TRUE.equals(s.getEnableKycRegistration()), Boolean.TRUE.equals(s.getEnable2fa()),
                Boolean.TRUE.equals(s.getGoogleTranslateEnabled()), Boolean.TRUE.equals(s.getUseCryptoFeature()),
                s.getTawkToEmbed()
        );
    }
}
