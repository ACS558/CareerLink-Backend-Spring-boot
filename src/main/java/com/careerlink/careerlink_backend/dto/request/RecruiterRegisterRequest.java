package com.careerlink.careerlink_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecruiterRegisterRequest(
        @NotBlank @Email(message = "Valid email is required") String email,
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password,
        @NotBlank(message = "Company name is required") String companyName,
        @NotBlank(message = "Industry is required") String industry,
        @NotBlank(message = "Location is required") String location,
        @NotBlank(message = "Contact person name is required") String contactName,
        @NotBlank(message = "Contact designation is required") String contactDesignation,
        @NotBlank(message = "Contact phone number is required") String contactPhone,
        @NotBlank @Email(message = "Valid contact email is required") String contactEmail
) {}
