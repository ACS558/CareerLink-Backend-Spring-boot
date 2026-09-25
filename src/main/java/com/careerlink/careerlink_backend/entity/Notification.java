package com.careerlink.careerlink_backend.entity;

import com.careerlink.careerlink_backend.entity.enums.NotificationPriority;
import com.careerlink.careerlink_backend.entity.enums.NotificationType;
import com.careerlink.careerlink_backend.entity.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private Role userRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    private String title;

    @Lob
    private String message;

    private Long relatedJobId;
    private Long relatedApplicationId;
    private Long relatedUserId;

    private boolean isRead = false;
    private LocalDateTime readAt;
    private String actionUrl;

    @Enumerated(EnumType.STRING)
    private NotificationPriority priority = NotificationPriority.MEDIUM;
}
