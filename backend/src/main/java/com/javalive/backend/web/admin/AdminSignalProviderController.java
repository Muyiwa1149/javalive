package com.javalive.backend.web.admin;

import com.javalive.backend.service.external.OnlineTraderApiClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Proxies the external trading-signals broadcast API, mirroring {@code Admin\SignalProvderController}
 * exactly. Per the standing decision, built correctly and wired but not exercised against a live
 * upstream — the migrated {@code merchant_key} is null, matching source's own never-configured state.
 *
 * <p>Source additionally pushes published signals/results to Telegram via a custom Notification
 * channel keyed on {@code telegram_bot_api} (also unconfigured in migrated data). That push is not
 * implemented here — same "wire the parts we can verify, don't fabricate the parts we can't" line
 * drawn for the other external integrations this phase.
 */
@RestController
@RequestMapping("/api/admin/signal-provider")
public class AdminSignalProviderController {

    private final OnlineTraderApiClient apiClient;

    public AdminSignalProviderController(OnlineTraderApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @GetMapping("/signals")
    public Map<String, Object> signals(@RequestParam(defaultValue = "1") int page) {
        return apiClient.get("/trading-signals", Map.of("page", page));
    }

    @PostMapping("/signals")
    public Map<String, Object> addSignal(@RequestBody Map<String, Object> body) {
        return apiClient.post("/post-signals", body);
    }

    @PostMapping("/signals/{id}/publish")
    public Map<String, Object> publishSignal(@PathVariable String id) {
        return apiClient.get("/publish-signals/" + id, Map.of());
    }

    @PutMapping("/signals/result")
    public Map<String, Object> updateResult(@RequestBody Map<String, Object> body) {
        return apiClient.post("/update-result", body);
    }

    @DeleteMapping("/signals/{id}")
    public Map<String, Object> deleteSignal(@PathVariable String id) {
        return apiClient.delete("/delete-signal/" + id);
    }

    @GetMapping("/subscribers")
    public Map<String, Object> subscribers() {
        return apiClient.get("/signal-subscribers", Map.of());
    }

    @GetMapping("/settings")
    public Map<String, Object> settings() {
        return apiClient.get("/signal-settings", Map.of());
    }

    @PutMapping("/settings")
    public Map<String, Object> saveSettings(@RequestBody Map<String, Object> body) {
        return apiClient.post("/save-signal-settings", body);
    }
}
