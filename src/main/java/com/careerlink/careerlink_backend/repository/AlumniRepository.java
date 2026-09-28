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
    WHERE (:status IS NULL OR CAST(a.verificationStatus AS string) = :status)
    AND (:branch IS NULL OR a.academicInfo.branch = :branch)
    AND (:graduationYear IS NULL OR a.academicInfo.graduationYear = :graduationYear)
    AND (LOWER(COALESCE(a.registrationNumber, '')) LIKE :pattern ESCAPE '\\'
      OR LOWER(COALESCE(a.personalInfo.firstName, '')) LIKE :pattern ESCAPE '\\'
      OR LOWER(COALESCE(a.personalInfo.lastName, '')) LIKE :pattern ESCAPE '\\')
    ORDER BY a.createdAt DESC
    """)
    List<Alumni> searchAlumni(
            @Param("pattern") String pattern,
            @Param("status") String status,
            @Param("branch") String branch,
            @Param("graduationYear") Integer graduationYear);

}