package com.careerlink.careerlink_backend.repository;

import com.careerlink.careerlink_backend.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByStudentId(Long studentId);
}