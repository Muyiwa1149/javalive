package com.javalive.backend.web.notification;

import com.javalive.backend.dto.notification.NotificationSummary;
import com.javalive.backend.security.UserPrincipal;
import com.javalive.backend.service.notification.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class UserNotificationController {

    private final NotificationService notificationService;

    public UserNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationSummary> index(@AuthenticationPrincipal UserPrincipal principal) {
        return notificationService.forUser(principal.getId()).stream().map(NotificationSummary::from).toList();
    }

    @GetMapping("/count")
    public Map<String, Long> count(@AuthenticationPrincipal UserPrincipal principal) {
        return Map.of("unread", notificationService.unreadCountForUser(principal.getId()));
    }

    @PostMapping("/{id}/mark-read")
    public NotificationSummary markRead(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return NotificationSummary.from(notificationService.markRead(id, principal.getId(), false));
    }

    @PostMapping("/mark-all-read")
    public void markAllRead(@AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAllReadForUser(principal.getId());
    }

    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        notificationService.delete(id, principal.getId(), false);
    }
}
