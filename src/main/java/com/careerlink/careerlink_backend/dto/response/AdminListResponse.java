package com.careerlink.careerlink_backend.dto.response;

public record AdminListResponse(
        Long id, String email, String firstName, String lastName, String department, String roleLevel
) {}
