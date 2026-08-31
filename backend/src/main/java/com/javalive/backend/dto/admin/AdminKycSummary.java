package com.javalive.backend.dto.admin;

import com.javalive.backend.entity.Kyc;

import java.time.LocalDateTime;

public record AdminKycSummary(
        Long id, Long userId, String userName, String userEmail, String firstName, String lastName,
        String phoneNumber, String dob, String address, String city, String state, String country,
        String socialMedia, String documentType, String frontImage, String backImage, String status,
        LocalDateTime createdAt
) {
    public static AdminKycSummary from(Kyc k) {
        return new AdminKycSummary(k.getId(), k.getUser().getId(), k.getUser().getName(), k.getUser().getEmail(),
                k.getFirstName(), k.getLastName(), k.getPhoneNumber(), k.getDob(), k.getAddress(), k.getCity(),
                k.getState(), k.getCountry(), k.getSocialMedia(), k.getDocumentType(), k.getFrontImage(),
                k.getBackImage(), k.getStatus(), k.getCreatedAt());
    }
}
