package com.javalive.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves migrated + newly uploaded media (logos, KYC docs, deposit proofs, avatars, etc.) from
 * local disk at {@code /storage/**}, mirroring the source app's `storage/app/public` public disk.
 * Full upload handling lands with the relevant Phase 4/6 features; this just wires up serving so
 * Phase 3 pages can render the already-migrated images.
 */
@Configuration
public class StorageConfig implements WebMvcConfigurer {

    @Value("${javalive.storage.public-path}")
    private String publicStoragePath;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        String location = publicStoragePath.endsWith("/") ? publicStoragePath : publicStoragePath + "/";
        registry.addResourceHandler("/storage/**").addResourceLocations("file:" + location);
    }
}
