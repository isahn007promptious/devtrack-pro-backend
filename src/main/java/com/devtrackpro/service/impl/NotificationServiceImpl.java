package com.devtrackpro.service.impl;

import com.devtrackpro.dto.NotificationResponse;
import com.devtrackpro.entity.Notification;
import com.devtrackpro.entity.User;
import com.devtrackpro.exception.ResourceNotFoundException;
import com.devtrackpro.repository.NotificationRepository;
import com.devtrackpro.repository.UserRepository;
import com.devtrackpro.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void notifyUser(Long userId, String type, String message, Long relatedEntityId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        Notification notification = Notification.builder()
                .user(user)
                .title(formatTitle(type))
                .message(message)
                .type(type)
                .relatedEntityId(relatedEntityId)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(String currentUserEmail, Pageable pageable) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return notificationRepository.findByUserId(user.getId(), pageable)
                .map(n -> NotificationResponse.builder()
                        .id(n.getId())
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .isRead(n.isRead())
                        .type(n.getType())
                        .relatedEntityId(n.getRelatedEntityId())
                        .createdAt(n.getCreatedAt())
                        .build());
    }

    @Override
    public void markAsRead(Long notificationId, String currentUserEmail) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getEmail().equalsIgnoreCase(currentUserEmail)) {
            throw new AccessDeniedException("Not authorized to access this notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Notification> unread = notificationRepository.findByUserIdAndIsRead(user.getId(), false);
        for (Notification n : unread) {
            n.setRead(true);
        }
        notificationRepository.saveAll(unread);
    }

    private String formatTitle(String type) {
        if (type == null) return "System Notification";
        switch (type) {
            case "TASK_ASSIGNED": return "Task Assigned";
            case "DEADLINE_NEAR": return "Deadline Approaching";
            case "MENTION": return "New Mention";
            case "COMMENT_ADDED": return "New Comment";
            default: return "Notification Alert";
        }
    }
}
