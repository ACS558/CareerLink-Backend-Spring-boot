package com.careerlink.careerlink_backend.repository;

import com.careerlink.careerlink_backend.entity.EmailOtp;
import com.careerlink.careerlink_backend.entity.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {
    Optional<EmailOtp> findByEmailAndPurpose(String email, OtpPurpose purpose);
    void deleteByEmailAndPurpose(String email, OtpPurpose purpose);
}
