package com.careerlink.careerlink_backend.dto.request;

import java.time.LocalDate;
import java.util.List;

public record AlumniProfileUpdateRequest(
        String firstName,
        String lastName,
        String phoneNumber,
        String currentCompany,
        String currentDesignation,
        String currentLocation,
        Integer experience,
        LocalDate startDate,
        String linkedin,
        String github,
        String portfolio,
        String twitter,
        List<ExternalLinkDto> externalLinks
) {}
