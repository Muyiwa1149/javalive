package com.javalive.backend.web.admin;

import com.javalive.backend.dto.admin.AdminCreateManagerRequest;
import com.javalive.backend.dto.admin.AdminEditManagerRequest;
import com.javalive.backend.dto.admin.AdminMailRequest;
import com.javalive.backend.dto.admin.AdminManagerSummary;
import com.javalive.backend.security.AdminPrincipal;
import com.javalive.backend.service.admin.AdminManageAdminService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/admins")
public class AdminManageAdminController {

    private final AdminManageAdminService adminManageAdminService;

    public AdminManageAdminController(AdminManageAdminService adminManageAdminService) {
        this.adminManageAdminService = adminManageAdminService;
    }

    @GetMapping
    public List<AdminManagerSummary> list(@AuthenticationPrincipal AdminPrincipal principal) {
        return adminManageAdminService.list(principal.getAdmin());
    }

    @PostMapping
    public AdminManagerSummary create(@AuthenticationPrincipal AdminPrincipal principal, @Valid @RequestBody AdminCreateManagerRequest request) {
        return adminManageAdminService.create(principal.getAdmin(), request);
    }

    @PutMapping("/{id}")
    public AdminManagerSummary edit(@AuthenticationPrincipal AdminPrincipal principal, @PathVariable Long id, @Valid @RequestBody AdminEditManagerRequest request) {
        return adminManageAdminService.edit(principal.getAdmin(), id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal AdminPrincipal principal, @PathVariable Long id) {
        adminManageAdminService.delete(principal.getAdmin(), id);
    }

    @PostMapping("/{id}/block")
    public void block(@AuthenticationPrincipal AdminPrincipal principal, @PathVariable Long id) {
        adminManageAdminService.block(principal.getAdmin(), id);
    }

    @PostMapping("/{id}/unblock")
    public void unblock(@AuthenticationPrincipal AdminPrincipal principal, @PathVariable Long id) {
        adminManageAdminService.unblock(principal.getAdmin(), id);
    }

    @PostMapping("/{id}/mail")
    public void mail(@AuthenticationPrincipal AdminPrincipal principal, @PathVariable Long id, @Valid @RequestBody AdminMailRequest request) {
        adminManageAdminService.sendMail(principal.getAdmin(), id, request.subject(), request.message());
    }
}
