package com.devtrackpro.repository;

import com.devtrackpro.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUserId(Long userId, Pageable pageable);
    List<Notification> findByUserIdAndIsRead(Long userId, boolean isRead);
    boolean existsByUserIdAndTypeAndRelatedEntityId(Long userId, String type, Long relatedEntityId);
}
