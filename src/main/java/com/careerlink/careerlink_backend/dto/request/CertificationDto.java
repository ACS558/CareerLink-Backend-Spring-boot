package com.careerlink.careerlink_backend.dto.request;

import java.time.LocalDate;

public record CertificationDto(
        String name,
        String issuedBy,
        LocalDate issueDate,
        String credentialUrl
) {}
