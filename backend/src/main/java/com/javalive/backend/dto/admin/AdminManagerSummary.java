package com.javalive.backend.dto.admin;

import com.javalive.backend.entity.Admin;

import java.time.LocalDateTime;

public record AdminManagerSummary(
        Long id, String firstName, String lastName, String email, String phone, String type,
        String status, boolean enable2fa, LocalDateTime createdAt
) {
    public static AdminManagerSummary from(Admin a) {
        return new AdminManagerSummary(a.getId(), a.getFirstName(), a.getLastName(), a.getEmail(), a.getPhone(),
                a.getType(), a.getStatus(), Boolean.TRUE.equals(a.getEnable2fa()), a.getCreatedAt());
    }
}
