package com.careerlink.careerlink_backend.repository;

import com.careerlink.careerlink_backend.entity.Admin;
import com.careerlink.careerlink_backend.entity.enums.RoleLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUserId(Long userId);
    boolean existsByRoleLevel(RoleLevel roleLevel);
    long countByRoleLevel(RoleLevel roleLevel);
}