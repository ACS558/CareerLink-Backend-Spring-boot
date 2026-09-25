package com.careerlink.careerlink_backend.dto.response;

import java.time.LocalDate;

public record StudentPlacementResponse(
        Long id, Long relatedApplicationId, String company, String jobTitle, Double packageOffered,
        LocalDate offerDate, LocalDate joiningDate, String status, boolean isPrimary
) {}
