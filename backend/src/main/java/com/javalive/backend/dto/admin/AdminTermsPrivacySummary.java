package com.javalive.backend.dto.admin;

import com.javalive.backend.entity.TermsPrivacy;

public record AdminTermsPrivacySummary(Long id, String description, boolean useTerms) {
    public static AdminTermsPrivacySummary from(TermsPrivacy t) {
        return new AdminTermsPrivacySummary(t.getId(), t.getDescription(), Boolean.TRUE.equals(t.getUseTerms()));
    }
}
