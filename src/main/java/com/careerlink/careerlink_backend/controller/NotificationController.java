package com.careerlink.careerlink_backend.controller;

import com.careerlink.careerlink_backend.dto.response.ApiResponse;
import com.careerlink.careerlink_backend.dto.response.NotificationResponse;
import com.careerlink.careerlink_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(Authentication auth, @PathVariable Long id) {
        notificationService.deleteNotification(auth, id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted", null));
    }

    @DeleteMapping("/clear-read")
    public ResponseEntity<ApiResponse<Void>> clearRead(Authentication auth) {
        notificationService.clearReadNotifications(auth);
        return ResponseEntity.ok(ApiResponse.success("Read notifications cleared", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
            Authentication auth,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getNotifications(auth, isRead, PageRequest.of(page, size))));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> unreadCount(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadCount(auth)));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Marked as read", null));
    }

    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(Authentication auth) {
        notificationService.markAllAsRead(auth);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }
}
