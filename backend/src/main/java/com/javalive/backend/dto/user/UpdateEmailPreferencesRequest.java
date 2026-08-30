package com.javalive.backend.dto.user;

public record UpdateEmailPreferencesRequest(
        boolean sendOtpEmail, boolean sendRoiEmail, boolean sendInvPlanEmail
) {
}
