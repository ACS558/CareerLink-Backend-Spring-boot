package com.careerlink.careerlink_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudentRegisterRequest(
        @NotBlank(message = "Registration number is required") String registrationNumber,
        @NotBlank @Email(message = "Valid email is required") String email,
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password
) {}
