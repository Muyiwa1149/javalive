package com.javalive.backend.dto.admin;

import com.javalive.backend.entity.Testimony;

public record AdminTestimonySummary(Long id, String name, String position, String whatIsSaid, String picture) {
    public static AdminTestimonySummary from(Testimony t) {
        return new AdminTestimonySummary(t.getId(), t.getName(), t.getPosition(), t.getWhatIsSaid(), t.getPicture());
    }
}
