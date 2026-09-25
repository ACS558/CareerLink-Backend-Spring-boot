package com.careerlink.careerlink_backend.dto.response;

public record RecruiterResponse(
        Long id,
        String email,
        String companyName,
        String industry,
        String location,
        String companyLogoUrl,
        String website,
        String companySize,
        String description,
        String contactName,
        String contactDesignation,
        String contactPhone,
        String contactEmail,
        String verificationStatus
) {}
