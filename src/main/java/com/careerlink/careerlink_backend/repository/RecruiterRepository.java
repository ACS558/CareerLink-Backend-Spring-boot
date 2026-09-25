package com.careerlink.careerlink_backend.repository;

import com.careerlink.careerlink_backend.entity.Recruiter;
import com.careerlink.careerlink_backend.entity.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecruiterRepository extends JpaRepository<Recruiter, Long> {
    Optional<Recruiter> findByUserId(Long userId);
    List<Recruiter> findByVerificationStatus(VerificationStatus status);

    @Query("""
        SELECT r FROM Recruiter r
        WHERE (:status IS NULL OR r.verificationStatus = :status)
        AND (:search IS NULL OR LOWER(r.companyInfo.companyName) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY r.createdAt DESC
        """)
    List<Recruiter> searchRecruiters(@Param("search") String search, @Param("status") VerificationStatus status);
}