package com.careerlink.careerlink_backend.service;

import com.careerlink.careerlink_backend.dto.request.ExtensionReviewRequest;
import com.careerlink.careerlink_backend.dto.response.ExtensionRequestResponse;
import com.careerlink.careerlink_backend.dto.response.ExtensionStatsResponse;
import com.careerlink.careerlink_backend.entity.Admin;
import com.careerlink.careerlink_backend.entity.ExtensionRequest;
import com.careerlink.careerlink_backend.entity.Student;
import com.careerlink.careerlink_backend.entity.enums.AccountStatus;
import com.careerlink.careerlink_backend.entity.enums.ExtensionStatus;
import com.careerlink.careerlink_backend.entity.enums.NotificationPriority;
import com.careerlink.careerlink_backend.entity.enums.NotificationType;
import com.careerlink.careerlink_backend.exception.ResourceNotFoundException;
import com.careerlink.careerlink_backend.exception.UnauthorizedException;
import com.careerlink.careerlink_backend.repository.AdminRepository;
import com.careerlink.careerlink_backend.repository.ExtensionRequestRepository;
import com.careerlink.careerlink_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExtensionRequestService {

    private final ExtensionRequestRepository extensionRequestRepository;
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public List<ExtensionRequestResponse> listRequests(String status) {
        List<ExtensionRequest> requests = (status != null && !status.isBlank())
                ? extensionRequestRepository.findByStatus(ExtensionStatus.valueOf(status.toUpperCase()))
                : extensionRequestRepository.findAll();

        return requests.stream()
                .sorted((a, b) -> b.getRequestedAt().compareTo(a.getRequestedAt()))
                .map(this::toResponse)
                .toList();
    }

    public ExtensionStatsResponse getStats() {
        List<ExtensionRequest> all = extensionRequestRepository.findAll();
        long pending = all.stream().filter(r -> r.getStatus() == ExtensionStatus.PENDING).count();
        long approved = all.stream().filter(r -> r.getStatus() == ExtensionStatus.APPROVED).count();
        long rejected = all.stream().filter(r -> r.getStatus() == ExtensionStatus.REJECTED).count();
        return new ExtensionStatsResponse(all.size(), pending, approved, rejected);
    }

    @Transactional
    public void reviewRequest(Authentication auth, Long studentId, Long requestId, ExtensionReviewRequest req) {
        Admin admin = currentAdmin(auth);

        ExtensionRequest request = extensionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Extension request not found"));

        if (!request.getStudent().getId().equals(studentId)) {
            throw new ResourceNotFoundException("Extension request not found for this student");
        }
        if (request.getStatus() != ExtensionStatus.PENDING) {
            throw new UnauthorizedException("This request has already been reviewed");
        }

        boolean approve = "approve".equalsIgnoreCase(req.action());
        if (!approve && !"reject".equalsIgnoreCase(req.action())) {
            throw new UnauthorizedException("Invalid action. Use approve or reject");
        }

        Student student = request.getStudent();

        request.setStatus(approve ? ExtensionStatus.APPROVED : ExtensionStatus.REJECTED);
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        extensionRequestRepository.save(request);

        if (approve) {
            int days = req.extensionDays() != null ? req.extensionDays() : 30;
            LocalDateTime currentExpiry = student.getExpiryDate() != null ? student.getExpiryDate() : LocalDateTime.now();
            LocalDateTime newExpiry = currentExpiry.plusDays(days);
            student.setExpiryDate(newExpiry);
            student.setDeletionScheduledAt(newExpiry.plusDays(90));

            if (student.getAccountStatus() == AccountStatus.EXPIRED) {
                student.setAccountStatus(AccountStatus.ACTIVE);
            }
        }

        notificationService.notifyUser(
                student.getUser(),
                approve ? NotificationType.EXTENSION_APPROVED : NotificationType.EXTENSION_REJECTED,
                approve ? "Extension Approved" : "Extension Rejected",
                approve
                        ? "Your account extension request has been approved."
                        : "Your account extension request was rejected.",
                null, null, NotificationPriority.HIGH, "/student/dashboard"
        );
    }

    private Admin currentAdmin(Authentication auth) {
        var user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return adminRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Admin profile not found"));
    }

    private ExtensionRequestResponse toResponse(ExtensionRequest r) {
        Student s = r.getStudent();
        String name = s.getPersonalInfo() != null
                ? (s.getPersonalInfo().getFirstName() + " " + s.getPersonalInfo().getLastName())
                : s.getRegistrationNumber();

        return new ExtensionRequestResponse(
                r.getId(), s.getId(), s.getRegistrationNumber(), name,
                s.getUser().getEmail(), r.getReason(), r.getStatus().name(),
                r.getRequestedAt(), r.getReviewedAt(),
                s.getAccountStatus().name(), s.getExpiryDate()
        );
    }
}
