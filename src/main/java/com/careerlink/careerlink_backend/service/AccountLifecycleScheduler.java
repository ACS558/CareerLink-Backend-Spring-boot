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

    // Daily at 2 AM: mark accounts past their expiry date as EXPIRED. Nothing is ever deleted.
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void expireOverdueAccounts() {
        LocalDateTime now = LocalDateTime.now();
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
                        "Your CareerLink account has expired. You can request an extension from your dashboard.",
                        null, null, NotificationPriority.HIGH, "/student/dashboard"
                );
            }
        }
        if (expired > 0) {
            log.info("Account lifecycle sweep: expired {} student account(s)", expired);
        }
    }
}