package com.careerlink.careerlink_backend.dto.request;

import java.time.LocalDateTime;
import java.util.List;

public record JobUpdateRequest(
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
        LocalDateTime applicationDeadline
) {}
