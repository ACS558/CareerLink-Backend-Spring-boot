package com.careerlink.careerlink_backend.repository;

import com.careerlink.careerlink_backend.entity.Alumni;
import com.careerlink.careerlink_backend.entity.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlumniRepository extends JpaRepository<Alumni, Long> {
    Optional<Alumni> findByUserId(Long userId);
    Optional<Alumni> findByRegistrationNumber(String registrationNumber);
    boolean existsByRegistrationNumber(String registrationNumber);
    List<Alumni> findByVerificationStatus(VerificationStatus status);

    @Query("""
        SELECT a FROM Alumni a
        WHERE (:status IS NULL OR a.verificationStatus = :status)
        AND (:branch IS NULL OR a.academicInfo.branch = :branch)
        AND (:graduationYear IS NULL OR a.academicInfo.graduationYear = :graduationYear)
        AND (:search IS NULL OR
             LOWER(a.registrationNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR
             LOWER(a.personalInfo.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR
             LOWER(a.personalInfo.lastName) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY a.createdAt DESC
        """)
    List<Alumni> searchAlumni(
            @Param("search") String search,
            @Param("status") VerificationStatus status,
            @Param("branch") String branch,
            @Param("graduationYear") Integer graduationYear);
}