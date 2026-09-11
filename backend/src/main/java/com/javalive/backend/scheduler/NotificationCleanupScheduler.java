package com.javalive.backend.scheduler;

import com.javalive.backend.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Java port of the source app's {@code notifications:cleanup} command (default {@code --days=30},
 * not read-only-scoped) — scheduled {@code ->weekly()} in Kernel.php. Source gates the delete behind
 * an interactive {@code $this->confirm()}, but Laravel's scheduler runs commands non-interactively,
 * so in the actual live/scheduled path that confirmation always auto-proceeds with its default
 * ({@code true}) — the delete always happens; no confirmation step needed here.
 */
@Component
public class NotificationCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationCleanupScheduler.class);
    private static final int RETENTION_DAYS = 30;

    private final NotificationRepository notificationRepository;

    public NotificationCleanupScheduler(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /** {@code ->weekly()} in Kernel.php. */
    @Scheduled(initialDelay = 90_000, fixedDelay = 7L * 24 * 60 * 60 * 1000)
    @Transactional
    public void cleanupOldNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(RETENTION_DAYS);
        int deleted = notificationRepository.deleteByCreatedAtBefore(cutoff);
        if (deleted > 0) {
            log.info("Successfully deleted {} old notifications.", deleted);
        }
    }
}
