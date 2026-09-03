package com.javalive.backend.web.admin;

import com.javalive.backend.service.settings.IpBlacklistService;
import com.javalive.backend.service.settings.SettingsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Mirrors source's {@code ClearCacheController@clearcache} (Laravel's view/config cache) — the
 * Java equivalent of "stale cached state" here is the in-memory caches this app actually has:
 * {@link SettingsService} and {@link IpBlacklistService}, both of which otherwise only refresh on
 * their own write paths and would miss an out-of-band DB change (e.g. a direct SQL edit).
 */
@RestController
@RequestMapping("/api/admin/cache")
public class AdminCacheController {

    private final SettingsService settingsService;
    private final IpBlacklistService ipBlacklistService;

    public AdminCacheController(SettingsService settingsService, IpBlacklistService ipBlacklistService) {
        this.settingsService = settingsService;
        this.ipBlacklistService = ipBlacklistService;
    }

    @PostMapping("/clear")
    public Map<String, String> clear() {
        settingsService.refresh();
        ipBlacklistService.refresh();
        return Map.of("message", "Cache cleared successfully.");
    }
}
