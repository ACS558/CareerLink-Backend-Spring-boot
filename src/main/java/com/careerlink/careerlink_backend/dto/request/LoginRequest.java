package com.careerlink.careerlink_backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email or registration number is required") String identifier,
        @NotBlank(message = "Password is required") String password
) {}
