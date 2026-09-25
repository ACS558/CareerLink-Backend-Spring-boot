package com.careerlink.careerlink_backend.entity;

import com.careerlink.careerlink_backend.entity.embeddable.AcademicInfo;
import com.careerlink.careerlink_backend.entity.embeddable.CurrentRole;
import com.careerlink.careerlink_backend.entity.embeddable.PersonalInfo;
import com.careerlink.careerlink_backend.entity.embeddable.SocialLinks;
import com.careerlink.careerlink_backend.entity.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "alumnis")
public class Alumni extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true)
    private String registrationNumber;

    @Embedded
    private PersonalInfo personalInfo;

    @Embedded
    private AcademicInfo academicInfo;

    @Embedded
    private CurrentRole currentRole;

    @Embedded
    private SocialLinks socialLinks;

    @OneToMany(mappedBy = "alumni", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExternalLink> externalLinks = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private Admin verifiedBy;

    private LocalDateTime verifiedAt;
    private String rejectionReason;
}
