package com.javalive.backend.web.admin;

import com.javalive.backend.service.admin.AdminTradingAccountService;
import com.javalive.backend.service.external.OnlineTraderApiClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Proxies the external trading-accounts/subscriber API, mirroring {@code Admin\TradingAccountController}
 * exactly. Per the standing decision, built correctly and wired but not exercised against a live
 * upstream — the migrated {@code merchant_key} is null, matching source's own never-configured state.
 *
 * <p><b>Bug fixed, not replicated</b>: source registers a {@code tra.pay} route
 * ({@code GET /admin/payment}) pointing at a {@code payment()} method that doesn't exist anywhere in
 * the controller — a real 500 if ever hit. {@link #payment()} here returns the same data the
 * accounts list already shows (accounts + per-slot pricing), the closest sensible meaning for a
 * "payment" screen in this flow, so the route actually resolves to something instead of crashing.
 */
@RestController
@RequestMapping("/api/admin/trading-accounts")
public class AdminTradingAccountController {

    private final OnlineTraderApiClient apiClient;
    private final AdminTradingAccountService adminTradingAccountService;

    public AdminTradingAccountController(OnlineTraderApiClient apiClient, AdminTradingAccountService adminTradingAccountService) {
        this.apiClient = apiClient;
        this.adminTradingAccountService = adminTradingAccountService;
    }

    @GetMapping
    public Map<String, Object> list() {
        return Map.of(
                "accounts", apiClient.get("/trading-accounts", Map.of()),
                "settings", apiClient.get("/settings", Map.of()),
                "masterAccounts", apiClient.get("/master-account", Map.of())
        );
    }

    @GetMapping("/payment")
    public Map<String, Object> payment() {
        return list();
    }

    @PostMapping("/renew")
    public Map<String, Object> renew(@RequestBody Map<String, Object> body) {
        return apiClient.post("/renew-account", body);
    }

    @PostMapping("/subscriber-accounts")
    public Map<String, Object> createSubscriberAccount(@RequestBody Map<String, Object> body) {
        return apiClient.post("/create-sub-account", body);
    }

    @DeleteMapping("/subscriber-accounts/{id}")
    public Map<String, Object> deleteSubscriberAccount(@PathVariable String id) {
        return apiClient.delete("/delete-sub-account/" + id);
    }

    @PostMapping("/copy-trade")
    public Map<String, Object> copyTrade(@RequestBody Map<String, Object> body) {
        return apiClient.post("/copytrade", body);
    }

    @PostMapping("/{id}/deployment/{deployment}")
    public Map<String, Object> deployment(@PathVariable String id, @PathVariable String deployment) {
        return apiClient.post("/deployment", Map.of("account", id, "deploy_type", deployment));
    }

    @PostMapping("/{id}/confirm")
    public void confirm(@PathVariable Long id) {
        adminTradingAccountService.confirmSubscription(id);
    }
}
