package com.javalive.backend.web.signal;

import com.javalive.backend.security.UserPrincipal;
import com.javalive.backend.service.external.OnlineTraderApiClient;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * User-facing subscriber view of the external SaaS forex signal broadcast + Telegram push, mirrors
 * {@code ViewsController@tradeSignals}. Distinct from {@link SignalPlanController}, which is the
 * unrelated local legacy signal-plan browsing feature (see docs/CANONICAL-MODULES.md — "Signals —
 * NOT a duplicate, both are in scope").
 *
 * <p>See {@link OnlineTraderApiClient}'s javadoc: not exercised against a real upstream response
 * (no configured merchant key in the migrated settings).
 */
@RestController
@RequestMapping("/api/signals/external")
public class SignalProviderController {

    private final OnlineTraderApiClient apiClient;

    public SignalProviderController(OnlineTraderApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @GetMapping("/subscription")
    public Map<String, Object> subscription(@AuthenticationPrincipal UserPrincipal principal) {
        return apiClient.get("/subscription", Map.of("id", principal.getId()));
    }

    @GetMapping("/settings")
    public Map<String, Object> settings() {
        return apiClient.get("/signal-settings", Map.of());
    }
}
