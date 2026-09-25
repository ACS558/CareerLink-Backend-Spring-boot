package com.careerlink.careerlink_backend.dto.response;

import com.careerlink.careerlink_backend.dto.request.CertificationDto;
import com.careerlink.careerlink_backend.dto.request.ExternalLinkDto;
import com.careerlink.careerlink_backend.dto.request.InternshipDto;
import com.careerlink.careerlink_backend.dto.request.ProjectDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record StudentResponse(
        Long id,
        String registrationNumber,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        LocalDate dateOfBirth,
        String gender,
        String department,
        String branch,
        Integer semester,
        Double cgpa,
        Double percentage,
        Integer backlogs,
        Integer graduationYear,
        List<String> skills,
        String resumeUrl,
        LocalDateTime resumeUploadedAt,
        String photoUrl,
        String linkedin,
        String github,
        String portfolio,
        String placementStatus,
        boolean profileCompleted,
        String accountStatus,
        List<ProjectDto> projects,
        List<InternshipDto> internships,
        List<CertificationDto> certifications,
        List<ExternalLinkDto> externalLinks
) {}
