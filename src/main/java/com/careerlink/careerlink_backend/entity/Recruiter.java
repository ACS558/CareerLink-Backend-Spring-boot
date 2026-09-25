package com.careerlink.careerlink_backend.entity;

import com.careerlink.careerlink_backend.entity.embeddable.CompanyInfo;
import com.careerlink.careerlink_backend.entity.embeddable.ContactPerson;
import com.careerlink.careerlink_backend.entity.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "recruiters")
public class Recruiter extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Embedded
    private CompanyInfo companyInfo;

    @Embedded
    private ContactPerson contactPerson;

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private Admin verifiedBy;

    private LocalDateTime verifiedAt;
    private String verificationNotes;
    private String rejectionReason;
}
