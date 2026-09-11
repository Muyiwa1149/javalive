package com.javalive.backend.repository;

import com.javalive.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByAdminIdOrderByCreatedAtDesc(Long adminId);

    List<Notification> findByUserIdAndIsRead(Long userId, Boolean isRead);

    long countByUserIdAndIsRead(Long userId, Boolean isRead);

    long countByAdminIdAndIsRead(Long adminId, Boolean isRead);

    @Modifying
    @Query("delete from Notification n where n.createdAt < :cutoff")
    int deleteByCreatedAtBefore(@Param("cutoff") LocalDateTime cutoff);
}
