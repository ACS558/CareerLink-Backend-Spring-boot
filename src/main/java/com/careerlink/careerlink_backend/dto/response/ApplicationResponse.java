package com.careerlink.careerlink_backend.dto.response;

import java.time.LocalDateTime;

public record ApplicationResponse(
        Long id,
        Long jobId,
        String jobTitle,
        String companyName,
        Long studentId,
        String studentName,
        String registrationNumber,
        String status,
        LocalDateTime appliedAt,
        AtsScoreResponse atsScore,
        String location,
        String jobType,
        String workMode,
        Double salaryMin,
        Double salaryMax,
        String salaryType,
        LocalDateTime shortlistedAt,
        LocalDateTime selectedAt,
        String rejectionReason,
        String recruiterNotes,
        String studentBranch,
        Double studentCgpa,
        Integer studentGraduationYear,
        String resumeUrl
) {}
