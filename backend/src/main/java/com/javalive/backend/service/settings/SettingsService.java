package com.javalive.backend.service.settings;

import com.javalive.backend.entity.AppSetting;
import com.javalive.backend.repository.AppSettingRepository;
import com.javalive.backend.web.exception.ApiException;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Wraps the single-row `app_settings` table (the PHP app's `Settings`/`SettingsCont`/`Paystack`/
 * `Cp_transaction` rows, merged — see the entity javadoc). Read constantly across nearly every
 * feature (payment gateway keys, referral %, KYC toggle, etc.), so it's cached in memory and only
 * re-read from the DB when {@link #update} is called — mirrors how the PHP app just did
 * `Settings::find(1)` cheaply from its own opcache/query cache, without a real invalidation story.
 */
@Service
public class SettingsService {

    private final AppSettingRepository repository;
    private final AtomicReference<AppSetting> cache = new AtomicReference<>();

    public SettingsService(AppSettingRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    void warmCache() {
        refresh();
    }

    public AppSetting get() {
        AppSetting current = cache.get();
        return current != null ? current : refresh();
    }

    public AppSetting update(Consumer<AppSetting> mutator) {
        AppSetting settings = repository.findById(1L)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "App settings row is missing."));
        mutator.accept(settings);
        settings.setUpdatedAt(LocalDateTime.now());
        AppSetting saved = repository.save(settings);
        cache.set(saved);
        return saved;
    }

    private AppSetting refresh() {
        AppSetting settings = repository.findById(1L)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "App settings row is missing."));
        cache.set(settings);
        return settings;
    }
}
