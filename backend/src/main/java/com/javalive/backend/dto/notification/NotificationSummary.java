package com.javalive.backend.dto.notification;

import com.javalive.backend.entity.Notification;

import java.time.LocalDateTime;

public record NotificationSummary(
        Long id, String title, String message, String type, boolean isRead,
        Long sourceId, String sourceType, LocalDateTime createdAt
) {
    public static NotificationSummary from(Notification n) {
        return new NotificationSummary(n.getId(), n.getTitle(), n.getMessage(), n.getType(),
                Boolean.TRUE.equals(n.getIsRead()), n.getSourceId(), n.getSourceType(), n.getCreatedAt());
    }
}
