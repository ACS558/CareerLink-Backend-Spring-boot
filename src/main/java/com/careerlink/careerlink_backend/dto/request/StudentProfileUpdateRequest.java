package com.careerlink.careerlink_backend.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import java.time.LocalDate;
import java.util.List;

public record StudentProfileUpdateRequest(
        String firstName,
        String lastName,
        String phoneNumber,
        LocalDate dateOfBirth,
        String gender,

        String department,
        String branch,
        Integer semester,
        @DecimalMin(value = "0.0") @DecimalMax(value = "10.0") Double cgpa,
        Double percentage,
        Integer backlogs,
        Integer graduationYear,

        List<String> skills,

        String linkedin,
        String github,
        String portfolio,

        List<ProjectDto> projects,
        List<InternshipDto> internships,
        List<CertificationDto> certifications,
        List<ExternalLinkDto> externalLinks
) {}
