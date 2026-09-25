package com.careerlink.careerlink_backend.repository;

import com.careerlink.careerlink_backend.entity.StudentPlacement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentPlacementRepository extends JpaRepository<StudentPlacement, Long> {
    List<StudentPlacement> findByStudentId(Long studentId);
    boolean existsByRelatedApplicationId(Long applicationId);
}