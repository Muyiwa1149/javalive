package com.javalive.backend.dto.admin;

import com.javalive.backend.entity.User;

import java.time.LocalDateTime;

public record AdminLeadSummary(
        Long id, String name, String email, String phone, String country, String assignedAgent, LocalDateTime createdAt
) {
    public static AdminLeadSummary from(User u) {
        return new AdminLeadSummary(u.getId(), u.getName(), u.getEmail(), u.getPhone(), u.getCountry(),
                u.getAssignedAgent(), u.getCreatedAt());
    }
}
