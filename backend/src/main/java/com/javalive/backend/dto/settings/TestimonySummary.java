package com.javalive.backend.dto.settings;

import com.javalive.backend.entity.Testimony;

public record TestimonySummary(Long id, String name, String position, String whatIsSaid, String picture) {
    public static TestimonySummary from(Testimony t) {
        return new TestimonySummary(t.getId(), t.getName(), t.getPosition(), t.getWhatIsSaid(), t.getPicture());
    }
}
