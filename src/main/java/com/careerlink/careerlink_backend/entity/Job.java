package com.careerlink.careerlink_backend.entity;

import com.careerlink.careerlink_backend.entity.enums.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "jobs")
public class Job extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_id", nullable = false)
    private Recruiter recruiter;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private JobType jobType = JobType.FULL_TIME;

    @Enumerated(EnumType.STRING)
    private WorkMode workMode = WorkMode.ON_SITE;

    private String location;
    private Double salaryMin;
    private Double salaryMax;

    @Enumerated(EnumType.STRING)
    private SalaryType salaryType = SalaryType.LPA;

    @ElementCollection
    @CollectionTable(name = "job_eligible_branches", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "branch")
    private List<String> eligibleBranches = new ArrayList<>();

    private Double minCgpa;
    private Integer maxBacklogs = 0;

    @ElementCollection
    @CollectionTable(name = "job_graduation_years", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "year")
    private List<Integer> graduationYears = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "job_skills_required", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "skill")
    private List<String> skillsRequired = new ArrayList<>();

    private Integer numberOfOpenings = 1;
    private LocalDateTime applicationDeadline;
    private boolean isActive = true;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Admin approvedBy;

    private LocalDateTime approvedAt;
    private String rejectionReason;
    private String approvalNotes;
}
