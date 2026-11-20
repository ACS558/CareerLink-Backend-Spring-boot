package com.careerlink.careerlink_backend.service;

import com.careerlink.careerlink_backend.entity.Alumni;
import com.careerlink.careerlink_backend.entity.Student;
import com.careerlink.careerlink_backend.entity.User;
import com.careerlink.careerlink_backend.exception.InvalidResetTokenException;
import com.careerlink.careerlink_backend.repository.AlumniRepository;
import com.careerlink.careerlink_backend.repository.StudentRepository;
import com.careerlink.careerlink_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private static final int EXPIRY_MINUTES = 30;
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final AlumniRepository alumniRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    @Value("${FRONTEND_URL:http://localhost:5173}")
    private String frontendUrl;

    @Transactional
    public void requestReset(String identifier) {
        findUser(identifier).ifPresent(user -> {
            if (recentlyRequested(user)) return; // asked less than a minute ago, do nothing

            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

            user.setResetPasswordToken(sha256(token));          // only the hash is stored
            user.setResetPasswordExpire(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES));
            userRepository.save(user);

            String link = frontendUrl + "/reset-password?token=" + token;
            emailService.sendPasswordResetLink(user.getEmail(), link, EXPIRY_MINUTES);
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetPasswordToken(sha256(token))
                .filter(u -> u.getResetPasswordExpire() != null
                        && u.getResetPasswordExpire().isAfter(LocalDateTime.now()))
                .filter(User::isActive)
                .orElseThrow(InvalidResetTokenException::new);

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);   // single use
        user.setResetPasswordExpire(null);
        userRepository.save(user);
    }

    private boolean recentlyRequested(User user) {
        if (user.getResetPasswordExpire() == null) return false;
        LocalDateTime issuedAt = user.getResetPasswordExpire().minusMinutes(EXPIRY_MINUTES);
        return issuedAt.plusSeconds(RESEND_COOLDOWN_SECONDS).isAfter(LocalDateTime.now());
    }

    private Optional<User> findUser(String identifier) {
        String id = identifier.trim();
        Optional<User> user;
        if (id.contains("@")) {
            user = userRepository.findByEmail(id);
        } else {
            String regNo = id.toUpperCase();
            user = studentRepository.findByRegistrationNumber(regNo).map(Student::getUser)
                    .or(() -> alumniRepository.findByRegistrationNumber(regNo).map(Alumni::getUser));
        }
        return user.filter(User::isActive);
    }

    private String sha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
