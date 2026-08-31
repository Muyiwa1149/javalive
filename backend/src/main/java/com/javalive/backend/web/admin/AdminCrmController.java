package com.javalive.backend.web.admin;

import com.javalive.backend.dto.admin.AdminAssignLeadRequest;
import com.javalive.backend.dto.admin.AdminLeadSummary;
import com.javalive.backend.dto.admin.AdminPickerSummary;
import com.javalive.backend.dto.admin.AdminTaskRequest;
import com.javalive.backend.dto.admin.AdminTaskSummary;
import com.javalive.backend.security.AdminPrincipal;
import com.javalive.backend.service.admin.AdminCrmService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/crm")
public class AdminCrmController {

    private final AdminCrmService adminCrmService;

    public AdminCrmController(AdminCrmService adminCrmService) {
        this.adminCrmService = adminCrmService;
    }

    @GetMapping("/admins")
    public List<AdminPickerSummary> adminPicker() {
        return adminCrmService.adminPicker();
    }

    @GetMapping("/tasks")
    public List<AdminTaskSummary> allTasks() {
        return adminCrmService.allTasks();
    }

    @GetMapping("/tasks/mine")
    public List<AdminTaskSummary> myTasks(@AuthenticationPrincipal AdminPrincipal principal) {
        return adminCrmService.myTasks(principal.getId());
    }

    @PostMapping("/tasks")
    public AdminTaskSummary createTask(@Valid @RequestBody AdminTaskRequest request) {
        return adminCrmService.createTask(request);
    }

    @PutMapping("/tasks/{id}")
    public AdminTaskSummary updateTask(@PathVariable Long id, @Valid @RequestBody AdminTaskRequest request) {
        return adminCrmService.updateTask(id, request);
    }

    @PostMapping("/tasks/{id}/done")
    public void markDone(@PathVariable Long id) {
        adminCrmService.markDone(id);
    }

    @DeleteMapping("/tasks/{id}")
    public void deleteTask(@PathVariable Long id) {
        adminCrmService.deleteTask(id);
    }

    @GetMapping("/leads")
    public List<AdminLeadSummary> leads() {
        return adminCrmService.leads();
    }

    @PostMapping("/leads/{userId}/convert")
    public void convert(@PathVariable Long userId) {
        adminCrmService.convertLead(userId);
    }

    @PostMapping("/leads/{userId}/assign")
    public void assign(@PathVariable Long userId, @Valid @RequestBody AdminAssignLeadRequest request) {
        adminCrmService.assignLead(userId, request.adminId());
    }
}
