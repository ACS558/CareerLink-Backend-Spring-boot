package com.careerlink.careerlink_backend.entity;

import com.careerlink.careerlink_backend.entity.embeddable.*;
import com.careerlink.careerlink_backend.entity.enums.AccountStatus;
import com.careerlink.careerlink_backend.entity.enums.PlacementStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    private static final int ACADEMIC_YEAR_START_MONTH = 6;

    private Integer academicYear;

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


    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExtensionRequest> extensionRequests = new ArrayList<>();


    @PrePersist
    public void computeLifecycleDates() {
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }
        academicYear = registrationDate.getMonthValue() >= ACADEMIC_YEAR_START_MONTH
                ? registrationDate.getYear()
                : registrationDate.getYear() - 1;
        careerGuidanceStartDate = registrationDate.plusDays(365);
        expiryDate = registrationDate.plusDays(375);
    }

    public void extendExpiry(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime base = (expiryDate != null && expiryDate.isAfter(now)) ? expiryDate : now;
        this.expiryDate = base.plusDays(days);
        if (this.accountStatus == AccountStatus.EXPIRED) {
            this.accountStatus = AccountStatus.ACTIVE;
        }
    }
}
