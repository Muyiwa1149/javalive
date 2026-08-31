package com.javalive.backend.web.admin;

import com.javalive.backend.dto.admin.AdminBotAnalytics;
import com.javalive.backend.dto.admin.AdminBotDetail;
import com.javalive.backend.dto.admin.AdminBotRequest;
import com.javalive.backend.dto.admin.AdminBotSummary;
import com.javalive.backend.dto.admin.AdminBotsDashboard;
import com.javalive.backend.service.admin.AdminBotService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/bots")
public class AdminBotController {

    private final AdminBotService adminBotService;

    public AdminBotController(AdminBotService adminBotService) {
        this.adminBotService = adminBotService;
    }

    @GetMapping
    public List<AdminBotSummary> list() {
        return adminBotService.list();
    }

    @GetMapping("/dashboard")
    public AdminBotsDashboard dashboard() {
        return adminBotService.dashboard();
    }

    @GetMapping("/{id}")
    public AdminBotDetail detail(@PathVariable Long id) {
        return adminBotService.detail(id);
    }

    @GetMapping("/{id}/analytics")
    public AdminBotAnalytics analytics(@PathVariable Long id) {
        return adminBotService.analytics(id);
    }

    @PostMapping(consumes = "multipart/form-data")
    public AdminBotSummary create(
            @RequestParam String name, @RequestParam String botType, @RequestParam String description,
            @RequestParam BigDecimal minInvestment, @RequestParam BigDecimal maxInvestment,
            @RequestParam BigDecimal dailyProfitMin, @RequestParam BigDecimal dailyProfitMax,
            @RequestParam Integer successRate, @RequestParam Integer durationDays,
            @RequestParam List<String> tradingPairs, @RequestParam String status,
            @RequestParam(required = false) MultipartFile image) {
        return adminBotService.create(new AdminBotRequest(name, botType, description, minInvestment, maxInvestment,
                dailyProfitMin, dailyProfitMax, successRate, durationDays, tradingPairs, status), image);
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public AdminBotSummary update(
            @PathVariable Long id,
            @RequestParam String name, @RequestParam String botType, @RequestParam String description,
            @RequestParam BigDecimal minInvestment, @RequestParam BigDecimal maxInvestment,
            @RequestParam BigDecimal dailyProfitMin, @RequestParam BigDecimal dailyProfitMax,
            @RequestParam Integer successRate, @RequestParam Integer durationDays,
            @RequestParam List<String> tradingPairs, @RequestParam String status,
            @RequestParam(required = false) MultipartFile image) {
        return adminBotService.update(id, new AdminBotRequest(name, botType, description, minInvestment, maxInvestment,
                dailyProfitMin, dailyProfitMax, successRate, durationDays, tradingPairs, status), image);
    }

    @PostMapping("/{id}/toggle")
    public void toggle(@PathVariable Long id) {
        adminBotService.toggleStatus(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        adminBotService.delete(id);
    }
}
