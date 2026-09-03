package com.javalive.backend.dto.admin;

import com.javalive.backend.entity.Content;

public record AdminContentSummary(Long id, String title, String description) {
    public static AdminContentSummary from(Content c) {
        return new AdminContentSummary(c.getId(), c.getTitle(), c.getDescription());
    }
}
