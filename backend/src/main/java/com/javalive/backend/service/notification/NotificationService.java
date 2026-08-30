package com.javalive.backend.service.notification;

import com.javalive.backend.entity.Admin;
import com.javalive.backend.entity.Notification;
import com.javalive.backend.entity.User;
import com.javalive.backend.repository.NotificationRepository;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/** Backs both the user-facing and admin-facing notification bells — same table, two audiences. */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification notifyUser(User user, String title, String message, String type) {
        return notifyUser(user, title, message, type, null, null);
    }

    public Notification notifyUser(User user, String title, String message, String type, Long sourceId, String sourceType) {
        Notification notification = Notification.builder()
                .user(user).title(title).message(message).type(type == null ? "info" : type)
                .isRead(false).sourceId(sourceId).sourceType(sourceType)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        return notificationRepository.save(notification);
    }

    public Notification notifyAdmin(Admin admin, String title, String message, String type) {
        Notification notification = Notification.builder()
                .admin(admin).title(title).message(message).type(type == null ? "info" : type)
                .isRead(false)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        return notificationRepository.save(notification);
    }

    public List<Notification> forUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> forAdmin(Long adminId) {
        return notificationRepository.findByAdminIdOrderByCreatedAtDesc(adminId);
    }

    public long unreadCountForUser(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    public long unreadCountForAdmin(Long adminId) {
        return notificationRepository.countByAdminIdAndIsRead(adminId, false);
    }

    public Notification markRead(Long notificationId, Long ownerId, boolean isAdmin) {
        Notification notification = getOwned(notificationId, ownerId, isAdmin);
        notification.setIsRead(true);
        notification.setUpdatedAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    public void markAllReadForUser(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsRead(userId, false);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    public void markAllReadForAdmin(Long adminId) {
        forAdmin(adminId).stream().filter(n -> !Boolean.TRUE.equals(n.getIsRead())).forEach(n -> n.setIsRead(true));
    }

    public void delete(Long notificationId, Long ownerId, boolean isAdmin) {
        Notification notification = getOwned(notificationId, ownerId, isAdmin);
        notificationRepository.delete(notification);
    }

    private Notification getOwned(Long notificationId, Long ownerId, boolean isAdmin) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification not found."));
        Long actualOwnerId = isAdmin
                ? (notification.getAdmin() != null ? notification.getAdmin().getId() : null)
                : (notification.getUser() != null ? notification.getUser().getId() : null);
        if (actualOwnerId == null || !actualOwnerId.equals(ownerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You don't have permission to access this notification.");
        }
        return notification;
    }
}
