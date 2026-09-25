package com.careerlink.careerlink_backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ApplicationStatusUpdateRequest(
        @NotBlank(message = "Status is required") String status, // SHORTLISTED | REJECTED | SELECTED | PENDING
        String rejectionReason,
        String recruiterNotes
) {}
