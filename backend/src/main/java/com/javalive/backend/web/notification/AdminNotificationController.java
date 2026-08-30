package com.javalive.backend.web.notification;

import com.javalive.backend.dto.notification.NotificationSummary;
import com.javalive.backend.security.AdminPrincipal;
import com.javalive.backend.service.notification.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Canonical notification controller per docs/CANONICAL-MODULES.md — the source app had two
 * parallel admin notification controllers; this one matches the endpoints the admin topbar
 * actually calls (`AdminNotificationController` in the PHP app was the unused duplicate).
 */
@RestController
@RequestMapping("/api/admin/notifications")
public class AdminNotificationController {

    private final NotificationService notificationService;

    public AdminNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationSummary> index(@AuthenticationPrincipal AdminPrincipal principal) {
        return notificationService.forAdmin(principal.getId()).stream().map(NotificationSummary::from).toList();
    }

    @GetMapping("/count")
    public Map<String, Long> count(@AuthenticationPrincipal AdminPrincipal principal) {
        return Map.of("unread", notificationService.unreadCountForAdmin(principal.getId()));
    }

    @PostMapping("/{id}/mark-read")
    public NotificationSummary markRead(@AuthenticationPrincipal AdminPrincipal principal, @PathVariable Long id) {
        return NotificationSummary.from(notificationService.markRead(id, principal.getId(), true));
    }

    @PostMapping("/mark-all-read")
    public void markAllRead(@AuthenticationPrincipal AdminPrincipal principal) {
        notificationService.markAllReadForAdmin(principal.getId());
    }

    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal AdminPrincipal principal, @PathVariable Long id) {
        notificationService.delete(id, principal.getId(), true);
    }
}
