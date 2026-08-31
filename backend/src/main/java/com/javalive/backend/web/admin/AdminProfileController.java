package com.javalive.backend.web.admin;

import com.javalive.backend.dto.admin.AdminChangeStyleRequest;
import com.javalive.backend.dto.admin.AdminUpdatePasswordRequest;
import com.javalive.backend.dto.admin.AdminUpdateProfileRequest;
import com.javalive.backend.dto.auth.AdminSummary;
import com.javalive.backend.security.AdminPrincipal;
import com.javalive.backend.service.admin.AdminProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/profile")
public class AdminProfileController {

    private final AdminProfileService adminProfileService;

    public AdminProfileController(AdminProfileService adminProfileService) {
        this.adminProfileService = adminProfileService;
    }

    @PutMapping
    public AdminSummary update(@AuthenticationPrincipal AdminPrincipal principal, @Valid @RequestBody AdminUpdateProfileRequest request) {
        return adminProfileService.updateProfile(principal.getId(), request.firstName(), request.lastName(), request.phone(), request.enable2fa());
    }

    @PutMapping("/password")
    public void updatePassword(@AuthenticationPrincipal AdminPrincipal principal, @Valid @RequestBody AdminUpdatePasswordRequest request) {
        adminProfileService.updatePassword(principal.getId(), request.oldPassword(), request.password(), request.passwordConfirmation());
    }

    @PutMapping("/style")
    public AdminSummary changeStyle(@AuthenticationPrincipal AdminPrincipal principal, @Valid @RequestBody AdminChangeStyleRequest request) {
        return adminProfileService.changeStyle(principal.getId(), request.dark());
    }
}
