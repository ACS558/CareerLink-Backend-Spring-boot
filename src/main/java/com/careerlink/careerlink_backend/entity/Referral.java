package com.careerlink.careerlink_backend.entity;

import com.careerlink.careerlink_backend.entity.enums.ApprovalStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "referrals")
public class Referral extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumni_id", nullable = false)
    private Alumni alumni;

    private String company;
    private String role;
    private String location;
    private String referralLink;

    @Column(columnDefinition = "TEXT")
    private String description;

    private boolean isActive = true;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Admin approvedBy;

    private LocalDateTime approvedAt;
    private String rejectionReason;
}
