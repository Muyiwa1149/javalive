package com.javalive.backend.dto.admin;

import com.javalive.backend.entity.CmsImage;

public record AdminCmsImageSummary(Long id, String title, String description, String imagePath) {
    public static AdminCmsImageSummary from(CmsImage i) {
        return new AdminCmsImageSummary(i.getId(), i.getTitle(), i.getDescription(), i.getImagePath());
    }
}
