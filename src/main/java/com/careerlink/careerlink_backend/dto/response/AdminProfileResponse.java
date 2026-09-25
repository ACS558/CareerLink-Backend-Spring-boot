package com.careerlink.careerlink_backend.dto.response;

public record AdminProfileResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String department,
        String phoneNumber,
        String roleLevel
) {}
