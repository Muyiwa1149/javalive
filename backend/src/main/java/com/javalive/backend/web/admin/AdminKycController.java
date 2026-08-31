package com.javalive.backend.web.admin;

import com.javalive.backend.dto.admin.AdminKycDecisionRequest;
import com.javalive.backend.dto.admin.AdminKycSummary;
import com.javalive.backend.service.admin.AdminKycService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/kyc")
public class AdminKycController {

    private final AdminKycService adminKycService;

    public AdminKycController(AdminKycService adminKycService) {
        this.adminKycService = adminKycService;
    }

    @GetMapping
    public List<AdminKycSummary> list() {
        return adminKycService.list();
    }

    @PostMapping("/{id}/decide")
    public void decide(@PathVariable Long id, @Valid @RequestBody AdminKycDecisionRequest request) {
        adminKycService.decide(id, request.action(), request.subject(), request.message());
    }
}
