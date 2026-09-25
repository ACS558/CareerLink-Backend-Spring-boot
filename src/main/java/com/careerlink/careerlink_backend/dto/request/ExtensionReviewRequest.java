package com.careerlink.careerlink_backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ExtensionReviewRequest(
        @NotBlank(message = "Action is required") String action, // "approve" or "reject"
        Integer extensionDays // default 30 if approved and not provided
) {}
