package com.careerlink.careerlink_backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record AutoShortlistRequest(
        @NotNull(message = "Threshold is required") Double threshold
) {}
