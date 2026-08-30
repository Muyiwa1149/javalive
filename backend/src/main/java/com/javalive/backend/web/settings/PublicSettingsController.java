package com.javalive.backend.web.settings;

import com.javalive.backend.dto.settings.PublicSettingsSummary;
import com.javalive.backend.service.settings.SettingsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/settings")
public class PublicSettingsController {

    private final SettingsService settingsService;

    public PublicSettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public PublicSettingsSummary get() {
        return PublicSettingsSummary.from(settingsService.get());
    }
}
