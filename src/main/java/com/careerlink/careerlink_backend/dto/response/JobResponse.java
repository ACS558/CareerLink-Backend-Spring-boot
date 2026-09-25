package com.careerlink.careerlink_backend.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record JobResponse(
        Long id,
        Long recruiterId,
        String recruiterEmail,
        String companyName,
        String companyLogoUrl,
        String industry,
        String companySize,
        String website,
        String companyDescription,
        String title,
        String description,
        String jobType,
        String workMode,
        String location,
        Double salaryMin,
        Double salaryMax,
        String salaryType,
        List<String> eligibleBranches,
        Double minCgpa,
        Integer maxBacklogs,
        List<Integer> graduationYears,
        List<String> skillsRequired,
        Integer numberOfOpenings,
        LocalDateTime applicationDeadline,
        boolean isActive,
        String approvalStatus,
        LocalDateTime createdAt
) {}
