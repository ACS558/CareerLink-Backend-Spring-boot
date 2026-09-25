package com.careerlink.careerlink_backend.repository;

import com.careerlink.careerlink_backend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByStudentId(Long studentId);
}