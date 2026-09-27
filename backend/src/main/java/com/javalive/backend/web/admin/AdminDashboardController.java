package com.javalive.backend.web.admin;

import com.javalive.backend.dto.admin.AdminDashboardSeries;
import com.javalive.backend.dto.admin.AdminDashboardSummary;
import com.javalive.backend.service.admin.AdminDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/summary")
    public AdminDashboardSummary summary() {
        return adminDashboardService.summary();
    }

    @GetMapping("/series")
    public AdminDashboardSeries series() {
        return adminDashboardService.series();
    }
}
