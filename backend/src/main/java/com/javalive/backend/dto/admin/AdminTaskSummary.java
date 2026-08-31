package com.javalive.backend.dto.admin;

import com.javalive.backend.entity.Task;

import java.time.LocalDate;

public record AdminTaskSummary(
        Long id, String title, String note, Long assignedAdminId, String assignedAdminName, LocalDate startDate,
        LocalDate endDate, String priority, String status
) {
    public static AdminTaskSummary from(Task t) {
        return new AdminTaskSummary(t.getId(), t.getTitle(), t.getNote(),
                t.getAssignedToAdmin() != null ? t.getAssignedToAdmin().getId() : null,
                t.getAssignedToAdmin() != null ? t.getAssignedToAdmin().getFirstName() + " " + t.getAssignedToAdmin().getLastName() : null,
                t.getStartDate(), t.getEndDate(), t.getPriority(), t.getStatus());
    }
}
