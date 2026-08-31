package com.javalive.backend.web.admin;

import com.javalive.backend.dto.admin.AdminCopyTradeSummary;
import com.javalive.backend.dto.admin.AdminCopyTradingStats;
import com.javalive.backend.dto.admin.AdminExpertRequest;
import com.javalive.backend.dto.admin.AdminExpertSummary;
import com.javalive.backend.service.admin.AdminCopyTradingService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/copy-trading")
public class AdminCopyTradingController {

    private final AdminCopyTradingService adminCopyTradingService;

    public AdminCopyTradingController(AdminCopyTradingService adminCopyTradingService) {
        this.adminCopyTradingService = adminCopyTradingService;
    }

    @GetMapping("/experts")
    public List<AdminExpertSummary> list() {
        return adminCopyTradingService.list();
    }

    @PostMapping(value = "/experts", consumes = "multipart/form-data")
    public AdminExpertSummary create(
            @RequestParam String name, @RequestParam(required = false) String tag, @RequestParam Integer rating,
            @RequestParam BigDecimal equity, @RequestParam BigDecimal totalProfit, @RequestParam Integer winRate,
            @RequestParam Integer totalTrades, @RequestParam BigDecimal price,
            @RequestParam(required = false) String description, @RequestParam String status,
            @RequestParam(required = false) MultipartFile photo) {
        return adminCopyTradingService.create(
                new AdminExpertRequest(name, tag, rating, equity, totalProfit, winRate, totalTrades, price, description, status),
                photo);
    }

    @PutMapping(value = "/experts/{id}", consumes = "multipart/form-data")
    public AdminExpertSummary update(
            @PathVariable Long id,
            @RequestParam String name, @RequestParam(required = false) String tag, @RequestParam Integer rating,
            @RequestParam BigDecimal equity, @RequestParam BigDecimal totalProfit, @RequestParam Integer winRate,
            @RequestParam Integer totalTrades, @RequestParam BigDecimal price,
            @RequestParam(required = false) String description, @RequestParam String status,
            @RequestParam(required = false) MultipartFile photo) {
        return adminCopyTradingService.update(id,
                new AdminExpertRequest(name, tag, rating, equity, totalProfit, winRate, totalTrades, price, description, status),
                photo);
    }

    @DeleteMapping("/experts/{id}")
    public void delete(@PathVariable Long id) {
        adminCopyTradingService.delete(id);
    }

    @GetMapping("/active-trades")
    public List<AdminCopyTradeSummary> activeTrades() {
        return adminCopyTradingService.activeTrades();
    }

    @GetMapping("/statistics")
    public AdminCopyTradingStats statistics() {
        return adminCopyTradingService.statistics();
    }
}
