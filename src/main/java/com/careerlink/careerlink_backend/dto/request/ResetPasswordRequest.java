package com.careerlink.careerlink_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String newPassword
) {}
