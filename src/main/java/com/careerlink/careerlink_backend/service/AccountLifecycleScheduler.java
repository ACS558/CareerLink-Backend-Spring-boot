package com.careerlink.careerlink_backend.service;

import com.careerlink.careerlink_backend.entity.Student;
import com.careerlink.careerlink_backend.entity.enums.AccountStatus;
import com.careerlink.careerlink_backend.entity.enums.NotificationPriority;
import com.careerlink.careerlink_backend.entity.enums.NotificationType;
import com.careerlink.careerlink_backend.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountLifecycleScheduler {

    private final StudentRepository studentRepository;
    private final NotificationService notificationService;

    // Runs daily at 2 AM server time
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void runLifecycleSweep() {
        LocalDateTime now = LocalDateTime.now();
        expireOverdueAccounts(now);
        softDeleteOverdueAccounts(now);
    }

    private void expireOverdueAccounts(LocalDateTime now) {
        List<Student> active = studentRepository.findByAccountStatus(AccountStatus.ACTIVE);
        int expired = 0;

        for (Student student : active) {
            if (student.getExpiryDate() != null && student.getExpiryDate().isBefore(now)) {
                student.setAccountStatus(AccountStatus.EXPIRED);
                studentRepository.save(student);
                expired++;

                notificationService.notifyUser(
                        student.getUser(), NotificationType.ACCOUNT_EXPIRED,
                        "Account Expired",
                        "Your CareerLink account has expired. Request an extension from your dashboard if you need more time.",
                        null, null, NotificationPriority.HIGH, "/student/dashboard"
                );
            }
        }
        if (expired > 0) {
            log.info("Account lifecycle sweep: expired {} student account(s)", expired);
        }
    }

    private void softDeleteOverdueAccounts(LocalDateTime now) {
        List<Student> expired = studentRepository.findByAccountStatus(AccountStatus.EXPIRED);
        int deleted = 0;

        for (Student student : expired) {
            if (student.getDeletionScheduledAt() != null
                    && student.getDeletionScheduledAt().isBefore(now)
                    && !student.isDeleted()) {
                student.setDeleted(true);
                student.setDeletedAt(now);
                student.setAccountStatus(AccountStatus.DELETED);
                studentRepository.save(student);
                deleted++;
            }
        }
        if (deleted > 0) {
            log.info("Account lifecycle sweep: soft-deleted {} student account(s)", deleted);
        }
    }
}