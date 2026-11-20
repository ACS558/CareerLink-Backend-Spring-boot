package com.careerlink.careerlink_backend.entity;

import com.careerlink.careerlink_backend.entity.enums.OtpPurpose;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_otps", uniqueConstraints = @UniqueConstraint(columnNames = {"email", "purpose"}))
@Getter
@Setter
@NoArgsConstructor
public class EmailOtp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OtpPurpose purpose;

    private String codeHash;
    private LocalDateTime expiresAt;
    private LocalDateTime lastSentAt;

    @Column(nullable = false)
    private int attempts = 0;

    @Column(nullable = false)
    private boolean verified = false;

    private LocalDateTime verifiedAt;
}
