package com.javalive.backend.web.admin;

import com.javalive.backend.dto.admin.AdminActivitySummary;
import com.javalive.backend.dto.admin.AdminCreateUserRequest;
import com.javalive.backend.dto.admin.AdminEmailSegmentRequest;
import com.javalive.backend.dto.admin.AdminImpersonateResponse;
import com.javalive.backend.dto.admin.AdminMailRequest;
import com.javalive.backend.dto.admin.AdminNotifyRequest;
import com.javalive.backend.dto.admin.AdminUserDetail;
import com.javalive.backend.dto.admin.AdminUserEditRequest;
import com.javalive.backend.dto.admin.AdminUserSummary;
import com.javalive.backend.service.admin.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public List<AdminUserSummary> list() {
        return adminUserService.list();
    }

    @PostMapping
    public void create(@Valid @RequestBody AdminCreateUserRequest request) {
        adminUserService.create(request.name(), request.username(), request.email(), request.password());
    }

    @PostMapping("/email-segment")
    public Map<String, Integer> emailSegment(@Valid @RequestBody AdminEmailSegmentRequest request) {
        int sent = adminUserService.emailSegment(request.category(), request.userIds(), request.subject(), request.message());
        return Map.of("sent", sent);
    }

    @GetMapping("/{id}")
    public AdminUserDetail detail(@PathVariable Long id) {
        return adminUserService.detail(id);
    }

    @PutMapping("/{id}")
    public AdminUserDetail edit(@PathVariable Long id, @Valid @RequestBody AdminUserEditRequest request) {
        return adminUserService.edit(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        adminUserService.delete(id);
    }

    @PostMapping("/{id}/block")
    public void block(@PathVariable Long id) {
        adminUserService.block(id);
    }

    @PostMapping("/{id}/unblock")
    public void unblock(@PathVariable Long id) {
        adminUserService.unblock(id);
    }

    @PostMapping("/{id}/verify-email")
    public void verifyEmail(@PathVariable Long id) {
        adminUserService.verifyEmail(id);
    }

    @PostMapping("/{id}/reset-password")
    public void resetPassword(@PathVariable Long id) {
        adminUserService.resetPassword(id);
    }

    @PostMapping("/{id}/impersonate")
    public AdminImpersonateResponse impersonate(@PathVariable Long id) {
        return new AdminImpersonateResponse(adminUserService.impersonate(id));
    }

    @GetMapping("/{id}/activity")
    public List<AdminActivitySummary> activity(@PathVariable Long id) {
        return adminUserService.activity(id);
    }

    @DeleteMapping("/{id}/activity")
    public void clearActivity(@PathVariable Long id) {
        adminUserService.clearActivity(id);
    }

    @PostMapping("/{id}/mail")
    public void mail(@PathVariable Long id, @Valid @RequestBody AdminMailRequest request) {
        adminUserService.sendMail(id, request.subject(), request.message());
    }

    @PostMapping("/{id}/notify")
    public void notify(@PathVariable Long id, @Valid @RequestBody AdminNotifyRequest request) {
        adminUserService.notifyDashboard(id, request.message());
    }
}
