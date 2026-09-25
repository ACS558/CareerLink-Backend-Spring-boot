package com.careerlink.careerlink_backend.repository;

import com.careerlink.careerlink_backend.entity.ExternalLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExternalLinkRepository extends JpaRepository<ExternalLink, Long> {
    List<ExternalLink> findByStudentId(Long studentId);
    List<ExternalLink> findByAlumniId(Long alumniId);
}