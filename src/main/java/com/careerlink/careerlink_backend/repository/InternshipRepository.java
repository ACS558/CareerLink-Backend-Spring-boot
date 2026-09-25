package com.careerlink.careerlink_backend.repository;

import com.careerlink.careerlink_backend.entity.Internship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternshipRepository extends JpaRepository<Internship, Long> {
    List<Internship> findByStudentId(Long studentId);
}