package com.careerlink.careerlink_backend.repository;

import com.careerlink.careerlink_backend.entity.Application;
import com.careerlink.careerlink_backend.entity.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByJobIdAndStudentId(Long jobId, Long studentId);
    Optional<Application> findByJobIdAndStudentId(Long jobId, Long studentId);

    List<Application> findByStudentIdOrderByAppliedAtDesc(Long studentId);

    // ATS-ranked applications for a recruiter reviewing a job
    List<Application> findByJobIdOrderByAtsScore_ScoreDesc(Long jobId);

    List<Application> findByRecruiterIdAndStatus(Long recruiterId, ApplicationStatus status);

    // Candidates eligible for auto-shortlist
    List<Application> findByJobIdAndAtsScore_ScoreGreaterThanEqual(Long jobId, Double threshold);

    @Modifying
    @Transactional
    @Query("UPDATE Application a SET a.status = :status, a.updatedAt = CURRENT_TIMESTAMP WHERE a.id IN :ids")
    int bulkUpdateStatus(@Param("ids") List<Long> ids, @Param("status") ApplicationStatus status);

    long countByStudentId(Long studentId);
    long countByStudentIdAndStatus(Long studentId, ApplicationStatus status);
    long countByJobId(Long jobId);
    long countByRecruiterId(Long recruiterId);
    long countByRecruiterIdAndStatus(Long recruiterId, ApplicationStatus status);
    long countByStatus(ApplicationStatus status);

    @Query("SELECT AVG(a.atsScore.score) FROM Application a WHERE a.atsScore.score IS NOT NULL")
    Double averageAtsScore();

    @Query("SELECT COUNT(a) FROM Application a WHERE a.appliedAt >= :since")
    long countAppliedSince(@Param("since") java.time.LocalDateTime since);

    List<Application> findByJobIdIn(List<Long> jobIds);

    @Query("""
        SELECT a FROM Application a
        WHERE (:status IS NULL OR a.status = :status)
        AND (:jobId IS NULL OR a.job.id = :jobId)
        AND (:studentId IS NULL OR a.student.id = :studentId)
        ORDER BY a.createdAt DESC
        """)
    List<Application> findAllFiltered(
            @Param("status") ApplicationStatus status,
            @Param("jobId") Long jobId,
            @Param("studentId") Long studentId);
}