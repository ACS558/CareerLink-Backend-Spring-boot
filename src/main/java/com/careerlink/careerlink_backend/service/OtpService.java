package com.careerlink.careerlink_backend.service;

import com.careerlink.careerlink_backend.entity.EmailOtp;
import com.careerlink.careerlink_backend.entity.enums.OtpPurpose;
import com.careerlink.careerlink_backend.exception.OtpException;
import com.careerlink.careerlink_backend.repository.EmailOtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {
    private static final int OTP_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;
    private static final int VERIFIED_VALID_MINUTES = 30;

    private final EmailOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public void sendOtp(String rawEmail, OtpPurpose purpose) {
        String email = normalize(rawEmail);
        LocalDateTime now = LocalDateTime.now();

        EmailOtp otp = otpRepository.findByEmailAndPurpose(email, purpose).orElseGet(() -> {
            EmailOtp o = new EmailOtp();
            o.setEmail(email);
            o.setPurpose(purpose);
            return o;
        });

        if (otp.getLastSentAt() != null
                && otp.getLastSentAt().plusSeconds(RESEND_COOLDOWN_SECONDS).isAfter(now)) {
            long wait = Duration.between(now, otp.getLastSentAt().plusSeconds(RESEND_COOLDOWN_SECONDS)).getSeconds() + 1;
            throw new OtpException("Please wait " + wait + " seconds before requesting another code",
                    HttpStatus.TOO_MANY_REQUESTS);
        }

        String code = String.format("%06d", random.nextInt(1_000_000));
        otp.setCodeHash(passwordEncoder.encode(code));
        otp.setExpiresAt(now.plusMinutes(OTP_MINUTES));
        otp.setLastSentAt(now);
        otp.setAttempts(0);
        otp.setVerified(false);
        otp.setVerifiedAt(null);
        otpRepository.save(otp);

        // If sending fails this throws, and the save above is rolled back (no cooldown is started).
        emailService.sendOtpEmail(email, code, OTP_MINUTES);
    }

    // noRollbackFor: a wrong guess must still be counted
    @Transactional(noRollbackFor = OtpException.class)
    public void verifyOtp(String rawEmail, OtpPurpose purpose, String code) {
        EmailOtp otp = otpRepository.findByEmailAndPurpose(normalize(rawEmail), purpose)
                .orElseThrow(this::invalidCode);

        if (otp.getCodeHash() == null || otp.getExpiresAt() == null
                || otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw invalidCode();
        }
        if (otp.getAttempts() >= MAX_ATTEMPTS) {
            throw new OtpException("Too many wrong attempts. Please request a new code.", HttpStatus.BAD_REQUEST);
        }
        if (!passwordEncoder.matches(code, otp.getCodeHash())) {
            otp.setAttempts(otp.getAttempts() + 1);
            otpRepository.save(otp);
            throw invalidCode();
        }

        otp.setVerified(true);
        otp.setVerifiedAt(LocalDateTime.now());
        otp.setCodeHash(null); // the code can't be used again
        otpRepository.save(otp);
    }

    public void requireVerified(String rawEmail, OtpPurpose purpose) {
        boolean ok = otpRepository.findByEmailAndPurpose(normalize(rawEmail), purpose)
                .filter(EmailOtp::isVerified)
                .filter(o -> o.getVerifiedAt() != null
                        && o.getVerifiedAt().plusMinutes(VERIFIED_VALID_MINUTES).isAfter(LocalDateTime.now()))
                .isPresent();
        if (!ok) {
            throw new OtpException("Please verify your email first", HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public void consume(String rawEmail, OtpPurpose purpose) {
        otpRepository.deleteByEmailAndPurpose(normalize(rawEmail), purpose);
    }

    private OtpException invalidCode() {
        return new OtpException("Invalid or expired code", HttpStatus.BAD_REQUEST);
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }
}
