package com.careerlink.careerlink_backend.repository;

import com.careerlink.careerlink_backend.entity.ExtensionRequest;
import com.careerlink.careerlink_backend.entity.enums.ExtensionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExtensionRequestRepository extends JpaRepository<ExtensionRequest, Long> {
    List<ExtensionRequest> findByStudentId(Long studentId);
    List<ExtensionRequest> findByStatus(ExtensionStatus status);

    boolean existsByStudentIdAndStatus(Long studentId, ExtensionStatus status);
}