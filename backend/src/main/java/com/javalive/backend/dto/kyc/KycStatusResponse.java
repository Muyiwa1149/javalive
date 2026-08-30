package com.javalive.backend.dto.kyc;

import com.javalive.backend.entity.Kyc;

import java.time.LocalDateTime;

public record KycStatusResponse(
        boolean submitted, String status, String documentType, LocalDateTime submittedAt
) {
    public static KycStatusResponse notSubmitted() {
        return new KycStatusResponse(false, null, null, null);
    }

    public static KycStatusResponse from(Kyc kyc) {
        return new KycStatusResponse(true, kyc.getStatus(), kyc.getDocumentType(), kyc.getCreatedAt());
    }
}
