package com.careerlink.careerlink_backend.dto.request;

import java.time.LocalDate;

public record InternshipDto(
        String companyName,
        String role,
        String duration,
        LocalDate startDate,
        LocalDate endDate,
        String description
) {}
