package com.careerlink.careerlink_backend.repository;

import com.careerlink.careerlink_backend.entity.Job;
import com.careerlink.careerlink_backend.entity.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByRecruiterId(Long recruiterId);
    Page<Job> findByApprovalStatus(ApprovalStatus status, Pageable pageable);
    List<Job> findByApprovalStatus(ApprovalStatus status);

    // Jobs visible to a given student: approved, active, and matching their branch/CGPA/backlog/grad year
    @Query("""
        SELECT DISTINCT j FROM Job j
        LEFT JOIN j.eligibleBranches b
        LEFT JOIN j.graduationYears gy
        WHERE j.approvalStatus = 'APPROVED'
        AND j.isActive = true
        AND (j.eligibleBranches IS EMPTY OR b = :branch)
        AND (j.minCgpa IS NULL OR :cgpa >= j.minCgpa)
        AND (:backlogs <= j.maxBacklogs)
        AND (j.graduationYears IS EMPTY OR gy = :graduationYear)
        ORDER BY j.createdAt DESC
        """)
    Page<Job> findEligibleJobsForStudent(
            @Param("branch") String branch,
            @Param("cgpa") Double cgpa,
            @Param("backlogs") Integer backlogs,
            @Param("graduationYear") Integer graduationYear,
            Pageable pageable);

    long countByApprovalStatus(ApprovalStatus status);

    @Query("SELECT j.recruiter.companyInfo.companyName, COUNT(a) FROM Job j JOIN Application a ON a.job = j WHERE a.status = 'SELECTED' GROUP BY j.recruiter.companyInfo.companyName")
    List<Object[]> countSelectionsByCompany();

    @Query("""
        SELECT j FROM Job j
        WHERE (:status IS NULL OR j.approvalStatus = :status)
        AND (:search IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY j.createdAt DESC
        """)
    List<Job> searchJobsForAdmin(@Param("search") String search, @Param("status") ApprovalStatus status);
}