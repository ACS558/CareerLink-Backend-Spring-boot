package com.careerlink.careerlink_backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReferralRequest(
        @NotBlank(message = "Company is required") String company,
        @NotBlank(message = "Role is required") String role,
        String location,
        @NotBlank(message = "Referral link is required") String referralLink,
        String description
) {}
