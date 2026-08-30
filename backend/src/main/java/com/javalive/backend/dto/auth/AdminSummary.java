package com.javalive.backend.dto.auth;

import com.javalive.backend.entity.Admin;

/** Safe-to-expose projection of {@link Admin} — never serialize the entity itself. */
public record AdminSummary(Long id, String firstName, String lastName, String email, String type, String dashboardStyle) {
    public static AdminSummary from(Admin admin) {
        return new AdminSummary(admin.getId(), admin.getFirstName(), admin.getLastName(), admin.getEmail(),
                admin.getType(), admin.getDashboardStyle());
    }
}
