package com.careerlink.careerlink_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkStatusUpdateRequest(
        @NotEmpty(message = "Application IDs are required") List<Long> applicationIds,
        @NotBlank(message = "Status is required") String status
) {}
