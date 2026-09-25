package com.careerlink.careerlink_backend.dto.response;

import com.careerlink.careerlink_backend.dto.request.ExternalLinkDto;

import java.time.LocalDate;
import java.util.List;

public record AlumniResponse(
        Long id,
        String registrationNumber,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String branch,
        Integer graduationYear,
        String currentCompany,
        String currentDesignation,
        String currentLocation,
        Integer experience,
        String linkedin,
        String github,
        String portfolio,
        String verificationStatus,
        String photoUrl,
        String twitter, LocalDate startDate, List<ExternalLinkDto> externalLinks
) {}