package com.javalive.backend.web.admin;

import com.javalive.backend.dto.admin.AdminInvestmentSummary;
import com.javalive.backend.dto.admin.AdminPlanRequest;
import com.javalive.backend.dto.admin.AdminPlanSummary;
import com.javalive.backend.service.admin.AdminPlanService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminPlanController {

    private final AdminPlanService adminPlanService;

    public AdminPlanController(AdminPlanService adminPlanService) {
        this.adminPlanService = adminPlanService;
    }

    @GetMapping("/plans")
    public List<AdminPlanSummary> list() {
        return adminPlanService.list();
    }

    @PostMapping("/plans")
    public AdminPlanSummary create(@Valid @RequestBody AdminPlanRequest request) {
        return adminPlanService.create(request);
    }

    @PutMapping("/plans/{id}")
    public AdminPlanSummary update(@PathVariable Long id, @Valid @RequestBody AdminPlanRequest request) {
        return adminPlanService.update(id, request);
    }

    @PostMapping("/plans/{id}/toggle")
    public AdminPlanSummary toggle(@PathVariable Long id) {
        return adminPlanService.toggleActive(id);
    }

    @DeleteMapping("/plans/{id}")
    public void delete(@PathVariable Long id) {
        adminPlanService.delete(id);
    }

    @GetMapping("/investments")
    public List<AdminInvestmentSummary> activeInvestments() {
        return adminPlanService.activeInvestments();
    }
}
