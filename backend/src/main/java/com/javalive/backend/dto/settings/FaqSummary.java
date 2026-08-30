package com.javalive.backend.dto.settings;

import com.javalive.backend.entity.Faq;

public record FaqSummary(Long id, String question, String answer) {
    public static FaqSummary from(Faq f) {
        return new FaqSummary(f.getId(), f.getQuestion(), f.getAnswer());
    }
}
