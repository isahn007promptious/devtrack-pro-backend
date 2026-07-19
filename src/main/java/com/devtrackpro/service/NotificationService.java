package com.devtrackpro.service;

import com.devtrackpro.dto.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    void notifyUser(Long userId, String type, String message, Long relatedEntityId);
    Page<NotificationResponse> getMyNotifications(String currentUserEmail, Pageable pageable);
    void markAsRead(Long notificationId, String currentUserEmail);
    void markAllAsRead(String currentUserEmail);
}
