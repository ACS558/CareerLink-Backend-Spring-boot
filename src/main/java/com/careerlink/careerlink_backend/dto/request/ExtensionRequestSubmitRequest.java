package com.careerlink.careerlink_backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ExtensionRequestSubmitRequest(
        @NotBlank(message = "Reason is required") String reason
) {}
