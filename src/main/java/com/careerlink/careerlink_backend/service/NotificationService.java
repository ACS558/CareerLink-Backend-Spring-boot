package com.careerlink.careerlink_backend.service;


import com.careerlink.careerlink_backend.dto.response.NotificationResponse;
import com.careerlink.careerlink_backend.entity.Admin;
import com.careerlink.careerlink_backend.entity.Notification;
import com.careerlink.careerlink_backend.entity.Student;
import com.careerlink.careerlink_backend.entity.User;
import com.careerlink.careerlink_backend.entity.enums.NotificationPriority;
import com.careerlink.careerlink_backend.entity.enums.NotificationType;
import com.careerlink.careerlink_backend.exception.ResourceNotFoundException;
import com.careerlink.careerlink_backend.exception.UnauthorizedException;
import com.careerlink.careerlink_backend.repository.AdminRepository;
import com.careerlink.careerlink_backend.repository.NotificationRepository;
import com.careerlink.careerlink_backend.repository.StudentRepository;
import com.careerlink.careerlink_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final StudentRepository studentRepository;

    @Transactional
    public void notifyUser(User recipient, NotificationType type, String title, String message,
                           Long relatedJobId, Long relatedApplicationId,
                           NotificationPriority priority, String actionUrl) {

        Notification notification = new Notification();
        notification.setUser(recipient);
        notification.setUserRole(recipient.getRole());
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedJobId(relatedJobId);
        notification.setRelatedApplicationId(relatedApplicationId);
        notification.setPriority(priority != null ? priority : NotificationPriority.MEDIUM);
        notification.setActionUrl(actionUrl);

        Notification saved = notificationRepository.save(notification);
        pushLive(recipient.getEmail(), saved);
    }

    // Broadcasts to every admin — used when a job/referral needs approval, or a new recruiter/alumni registers.
    @Transactional
    public void notifyAllAdmins(NotificationType type, String title, String message, String actionUrl) {
        for (Admin admin : adminRepository.findAll()) {
            notifyUser(admin.getUser(), type, title, message, null, null, NotificationPriority.MEDIUM, actionUrl);
        }
    }

    private void pushLive(String email, Notification saved) {
        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(saved.getUser().getId());
        NotificationResponse payload = toResponse(saved, unreadCount);
        messagingTemplate.convertAndSendToUser(email, "/queue/notifications", payload);
    }

//    public Page<NotificationResponse> getNotifications(Authentication auth, Pageable pageable) {
//        User user = currentUser(auth);
//        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(user.getId());
//        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
//                .map(n -> toResponse(n, unreadCount));
//    }

    public Page<NotificationResponse> getNotifications(Authentication auth, Boolean isRead, Pageable pageable) {
        User user = currentUser(auth);
        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(user.getId());
        Page<Notification> page = (isRead != null)
                ? notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(user.getId(), isRead, pageable)
                : notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        return page.map(n -> toResponse(n, unreadCount));
    }

    public long getUnreadCount(Authentication auth) {
        return notificationRepository.countByUserIdAndIsReadFalse(currentUser(auth).getId());
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId);
    }

    @Transactional
    public void markAllAsRead(Authentication auth) {
        notificationRepository.markAllAsRead(currentUser(auth).getId());
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private NotificationResponse toResponse(Notification n, long unreadCount) {
        return new NotificationResponse(
                n.getId(), n.getType().name(), n.getTitle(), n.getMessage(),
                n.getRelatedJobId(), n.getRelatedApplicationId(), n.isRead(),
                n.getActionUrl(), n.getPriority().name(), n.getCreatedAt(), unreadCount
        );
    }
    @Transactional
    public void notifyAllStudents(NotificationType type, String title, String message, String actionUrl) {
        for (Student student : studentRepository.findAll()) {
            notifyUser(student.getUser(), type, title, message, null, null, NotificationPriority.LOW, actionUrl);
        }
    }

    @Transactional
    public void deleteNotification(Authentication auth, Long notificationId) {
        User user = currentUser(auth);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Not authorized to delete this notification");
        }
        notificationRepository.delete(notification);
    }

    @Transactional
    public void clearReadNotifications(Authentication auth) {
        User user = currentUser(auth);
        notificationRepository.deleteByUserIdAndIsReadTrue(user.getId());
    }
}
