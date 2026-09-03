package com.javalive.backend.web.admin;

import com.javalive.backend.service.external.OnlineTraderApiClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Proxies the external copy-trade master-account API, mirroring {@code Admin\SubscriptionController}
 * exactly. Per the standing decision, built correctly and wired but not exercised against a live
 * upstream — the migrated {@code merchant_key} is null, matching source's own never-configured state.
 */
@RestController
@RequestMapping("/api/admin/subscription")
public class AdminSubscriptionController {

    private final OnlineTraderApiClient apiClient;

    public AdminSubscriptionController(OnlineTraderApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @GetMapping("/trading-settings")
    public Map<String, Object> tradingSettings() {
        return Map.of(
                "account", apiClient.get("/account-profile", Map.of()),
                "masterAccounts", apiClient.get("/master-account", Map.of()),
                "tradingAccounts", apiClient.get("/trading-accounts", Map.of()),
                "settings", apiClient.get("/settings", Map.of())
        );
    }

    @PostMapping("/master-accounts")
    public Map<String, Object> createMasterAccount(@RequestBody Map<String, Object> body) {
        return apiClient.post("/create-copytrade-account", body);
    }

    @DeleteMapping("/master-accounts/{id}")
    public Map<String, Object> deleteMasterAccount(@PathVariable String id) {
        return apiClient.delete("/delete-master-account/" + id);
    }

    @PostMapping("/master-accounts/renew")
    public Map<String, Object> renewAccount(@RequestBody Map<String, Object> body) {
        return apiClient.post("/renew-master-account", body);
    }

    @PostMapping("/strategy")
    public Map<String, Object> updateStrategy(@RequestBody Map<String, Object> body) {
        return apiClient.post("/update-strategy", body);
    }
}
