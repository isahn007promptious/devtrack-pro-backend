package com.devtrackpro.controller;

import com.devtrackpro.dto.NotificationResponse;
import com.devtrackpro.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Endpoints for user in-app notification retrieval and read states")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Get current user's paginated notifications (sorted by most recent)")
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<NotificationResponse> response = notificationService.getMyNotifications(principal.getName(), pageable);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read by ID")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long id, Principal principal) {
        notificationService.markAsRead(id, principal.getName());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Notification marked as read successfully.");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all unread notifications of current user as read")
    public ResponseEntity<Map<String, String>> markAllAsRead(Principal principal) {
        notificationService.markAllAsRead(principal.getName());
        Map<String, String> response = new HashMap<>();
        response.put("message", "All notifications marked as read successfully.");
        return ResponseEntity.ok(response);
    }
}
