package com.javalive.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Backs {@code @Async} mail sending — outbound SMTP (real Gmail relay in prod) was blocking the
 * request thread on every login/deposit/withdrawal/etc., adding one full SMTP round-trip per
 * recipient (the user plus every admin) to the user-facing response. Small pool since this VPS
 * runs on 1.9GB total memory alongside the JVM heap, MariaDB, and nginx.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "mailExecutor")
    public Executor mailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("mail-async-");
        executor.initialize();
        return executor;
    }
}
