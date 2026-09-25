package com.careerlink.careerlink_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCreateRequest(
        @NotBlank @Email(message = "Valid email is required") String email,
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password,
        @NotBlank(message = "Name is required") String name,
        String department
) {}
