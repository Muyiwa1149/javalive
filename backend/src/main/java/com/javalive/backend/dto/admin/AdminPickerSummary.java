package com.javalive.backend.dto.admin;

import com.javalive.backend.entity.Admin;

/** Minimal admin projection safe to expose to any authenticated admin, for "assign to" pickers. */
public record AdminPickerSummary(Long id, String name) {
    public static AdminPickerSummary from(Admin a) {
        return new AdminPickerSummary(a.getId(), a.getFirstName() + " " + a.getLastName());
    }
}
