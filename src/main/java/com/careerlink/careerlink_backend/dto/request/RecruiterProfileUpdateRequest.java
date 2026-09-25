package com.careerlink.careerlink_backend.dto.request;

public record RecruiterProfileUpdateRequest(
        String companyName,
        String industry,
        String location,
        String website,
        String companySize,
        String description,
        String contactName,
        String contactDesignation,
        String contactPhone,
        String contactEmail
) {}