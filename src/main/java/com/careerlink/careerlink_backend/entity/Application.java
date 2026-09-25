package com.careerlink.careerlink_backend.entity;

import com.careerlink.careerlink_backend.entity.embeddable.AtsScore;
import com.careerlink.careerlink_backend.entity.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "applications",
        uniqueConstraints = @UniqueConstraint(columnNames = {"job_id", "student_id"})
)
public class Application extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_id", nullable = false)
    private Recruiter recruiter;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(columnDefinition = "TEXT", length = 5000)
    private String coverLetter;

    private LocalDateTime appliedAt = LocalDateTime.now();

    @Embedded
    private AtsScore atsScore;

    private LocalDateTime shortlistedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shortlisted_by")
    private User shortlistedBy;

    private LocalDateTime rejectedAt;
    private String rejectionReason;
    private LocalDateTime selectedAt;

    @Column(columnDefinition = "TEXT", length = 5000)
    private String recruiterNotes;
}
