package com.javalive.backend.dto.admin;

import com.javalive.backend.entity.Activity;

import java.time.LocalDateTime;

public record AdminActivitySummary(Long id, String ipAddress, String device, String browser, String os, LocalDateTime createdAt) {
    public static AdminActivitySummary from(Activity a) {
        return new AdminActivitySummary(a.getId(), a.getIpAddress(), a.getDevice(), a.getBrowser(), a.getOs(), a.getCreatedAt());
    }
}
