package com.careerlink.careerlink_backend.repository;

import com.careerlink.careerlink_backend.entity.Student;
import com.careerlink.careerlink_backend.entity.enums.AccountStatus;
import com.careerlink.careerlink_backend.entity.enums.PlacementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByUserId(Long userId);
    Optional<Student> findByRegistrationNumber(String registrationNumber);
    boolean existsByRegistrationNumber(String registrationNumber);

    List<Student> findByAccountStatus(AccountStatus status);

    Page<Student> findByAcademicInfoBranchContainingIgnoreCaseOrRegistrationNumberContainingIgnoreCase(
            String branch, String registrationNumber, Pageable pageable);

    long countByPlacementStatus(PlacementStatus status);

    @Query("SELECT s.academicInfo.branch, COUNT(s) FROM Student s GROUP BY s.academicInfo.branch")
    List<Object[]> countStudentsByBranch();

    @Query("SELECT s.academicInfo.branch, COUNT(s) FROM Student s WHERE s.placementStatus = 'PLACED' GROUP BY s.academicInfo.branch")
    List<Object[]> countPlacedStudentsByBranch();

    @Query("SELECT AVG(s.placedPackage) FROM Student s WHERE s.placementStatus = 'PLACED'")
    Double averagePlacedPackage();

    @Query("SELECT MAX(s.placedPackage) FROM Student s WHERE s.placementStatus = 'PLACED'")
    Double highestPlacedPackage();

    @Query("""
        SELECT s FROM Student s
        WHERE s.academicInfo.branch = COALESCE(CAST(:branch AS string), s.academicInfo.branch)
        AND CAST(s.placementStatus AS string) = COALESCE(CAST(:placementStatus AS string), CAST(s.placementStatus AS string))
        AND s.academicInfo.graduationYear = COALESCE(CAST(:graduationYear AS integer), s.academicInfo.graduationYear)
        AND (:search IS NULL OR
             LOWER(s.registrationNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR
             LOWER(s.personalInfo.firstName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR
             LOWER(s.personalInfo.lastName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        ORDER BY s.academicInfo.cgpa DESC
        """)
    Page<Student> searchStudents(
            @Param("search") String search,
            @Param("branch") String branch,
            @Param("placementStatus") String placementStatus, // now String, not PlacementStatus enum
            @Param("graduationYear") Integer graduationYear,
            Pageable pageable);

    @Query("SELECT MIN(s.placedPackage) FROM Student s WHERE s.placementStatus = 'PLACED'")
    Double lowestPlacedPackage();
}
