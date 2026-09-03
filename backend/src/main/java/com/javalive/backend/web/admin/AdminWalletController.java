package com.javalive.backend.web.admin;

import com.javalive.backend.dto.admin.AdminWalletSettingsRequest;
import com.javalive.backend.dto.admin.AdminWalletSettingsSummary;
import com.javalive.backend.dto.admin.AdminWalletSummary;
import com.javalive.backend.service.admin.AdminWalletService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/wallet-connect")
public class AdminWalletController {

    private final AdminWalletService adminWalletService;

    public AdminWalletController(AdminWalletService adminWalletService) {
        this.adminWalletService = adminWalletService;
    }

    @GetMapping
    public List<AdminWalletSummary> list() {
        return adminWalletService.list();
    }

    @PostMapping("/{id}/reveal")
    public Map<String, String> reveal(@PathVariable Long id) {
        return adminWalletService.revealPhrase(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        adminWalletService.delete(id);
    }

    @GetMapping("/settings")
    public AdminWalletSettingsSummary getSettings() {
        return adminWalletService.getSettings();
    }

    @PutMapping("/settings")
    public AdminWalletSettingsSummary updateSettings(@Valid @RequestBody AdminWalletSettingsRequest request) {
        return adminWalletService.updateSettings(request);
    }
}
