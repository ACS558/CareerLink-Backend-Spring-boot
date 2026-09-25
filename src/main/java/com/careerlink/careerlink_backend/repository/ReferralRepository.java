package com.careerlink.careerlink_backend.repository;

import com.careerlink.careerlink_backend.entity.Referral;
import com.careerlink.careerlink_backend.entity.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferralRepository extends JpaRepository<Referral, Long> {
    List<Referral> findByAlumniId(Long alumniId);
    Page<Referral> findByApprovalStatusAndIsActiveTrue(ApprovalStatus status, Pageable pageable);
    List<Referral> findByApprovalStatus(ApprovalStatus status);
    List<Referral> findAllByOrderByCreatedAtDesc();
}