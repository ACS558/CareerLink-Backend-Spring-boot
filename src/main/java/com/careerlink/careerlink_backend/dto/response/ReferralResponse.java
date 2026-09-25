package com.careerlink.careerlink_backend.dto.response;

import java.time.LocalDateTime;

public record ReferralResponse(
        Long id,
        String company,
        String role,
        String location,
        String referralLink,
        String description,
        String alumniName,
        String approvalStatus,
        LocalDateTime createdAt
) {}
