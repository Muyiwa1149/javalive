package com.javalive.backend.web.dashboard;

import com.javalive.backend.dto.dashboard.DashboardSummary;
import com.javalive.backend.security.UserPrincipal;
import com.javalive.backend.service.dashboard.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public DashboardSummary summary(@AuthenticationPrincipal UserPrincipal principal) {
        return dashboardService.getSummary(principal.getId());
    }
}
