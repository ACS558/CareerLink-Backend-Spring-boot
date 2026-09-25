package com.careerlink.careerlink_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record JobCreateRequest(
        @NotBlank(message = "Title is required") String title,
        @NotBlank(message = "Description is required") String description,
        String jobType,       // FULL_TIME | PART_TIME | INTERNSHIP | CONTRACT
        String workMode,      // ON_SITE | REMOTE | HYBRID
        String location,
        Double salaryMin,
        Double salaryMax,
        String salaryType,    // LPA | MONTHLY | HOURLY
        List<String> eligibleBranches,
        Double minCgpa,
        Integer maxBacklogs,
        List<Integer> graduationYears,
        List<String> skillsRequired,
        Integer numberOfOpenings,
        @NotNull(message = "Application deadline is required") LocalDateTime applicationDeadline
) {}
