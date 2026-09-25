package com.careerlink.careerlink_backend.entity;

import com.careerlink.careerlink_backend.entity.embeddable.*;
import com.careerlink.careerlink_backend.entity.enums.AccountStatus;
import com.careerlink.careerlink_backend.entity.enums.PlacementStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "students")
public class Student extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true)
    private String registrationNumber;

    @Embedded
    private PersonalInfo personalInfo;

    @Embedded
    private AcademicInfo academicInfo;

    @ElementCollection
    @CollectionTable(name = "student_skills", joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "url", column = @Column(name = "resume_url")),
            @AttributeOverride(name = "publicId", column = @Column(name = "resume_public_id")),
            @AttributeOverride(name = "uploadedAt", column = @Column(name = "resume_uploaded_at"))
    })
    private CloudinaryFile resume;

    @Column(name = "resume_text", columnDefinition = "TEXT", length = 5000)
    private String resumeText;


    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Project> projects = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Internship> internships = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Certification> certifications = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExternalLink> externalLinks = new ArrayList<>();

    @Embedded
    private SocialLinks socialLinks;

    @Enumerated(EnumType.STRING)
    private PlacementStatus placementStatus = PlacementStatus.UNPLACED;

    private String placedCompany;
    private Double placedPackage;
    private LocalDateTime placedAt;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentPlacement> placements = new ArrayList<>();

    @Column(nullable = false)
    private boolean profileCompleted = false;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDateTime registrationDate;

    private LocalDateTime careerGuidanceStartDate;
    private LocalDateTime expiryDate;
    private LocalDateTime deletionScheduledAt;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExtensionRequest> extensionRequests = new ArrayList<>();

    private boolean isDeleted = false;
    private LocalDateTime deletedAt;

    @PrePersist
    public void computeLifecycleDates() {
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }
        careerGuidanceStartDate = registrationDate.plusDays(365);
        expiryDate = registrationDate.plusDays(375);
        deletionScheduledAt = registrationDate.plusDays(465);
    }
}
