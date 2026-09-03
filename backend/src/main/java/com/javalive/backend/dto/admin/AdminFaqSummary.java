package com.javalive.backend.dto.admin;

import com.javalive.backend.entity.Faq;

public record AdminFaqSummary(Long id, String question, String answer) {
    public static AdminFaqSummary from(Faq f) {
        return new AdminFaqSummary(f.getId(), f.getQuestion(), f.getAnswer());
    }
}
