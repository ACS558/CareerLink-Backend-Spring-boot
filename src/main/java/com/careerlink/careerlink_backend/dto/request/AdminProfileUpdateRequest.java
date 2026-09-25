package com.careerlink.careerlink_backend.dto.request;

public record AdminProfileUpdateRequest(
        String firstName,
        String lastName,
        String department,
        String phoneNumber
) {}
