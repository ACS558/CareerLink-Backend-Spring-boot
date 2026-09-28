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
        WHERE CAST(a.verificationStatus AS string) = COALESCE(CAST(:status AS string), CAST(a.verificationStatus AS string))
        AND a.academicInfo.branch = COALESCE(CAST(:branch AS string), a.academicInfo.branch)
        AND a.academicInfo.graduationYear = COALESCE(CAST(:graduationYear AS integer), a.academicInfo.graduationYear)
        AND (:search IS NULL OR
             LOWER(a.registrationNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR
             LOWER(a.personalInfo.firstName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR
             LOWER(a.personalInfo.lastName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        ORDER BY a.createdAt DESC
        """)
    List<Alumni> searchAlumni(
            @Param("search") String search,
            @Param("status") String status,
            @Param("branch") String branch,
            @Param("graduationYear") Integer graduationYear);
}