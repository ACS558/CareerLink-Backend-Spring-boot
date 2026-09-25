package com.careerlink.careerlink_backend.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record StudentDashboardResponse(
        String dashboardMode, // "career_guidance" or "normal"
        long daysUntilExpiry,
        String accountStatus,
        String placementStatus,
        StudentInfo student
) {
    public record StudentInfo(
            String name, String email, String registrationNumber,
            String placedCompany, Double placedPackage,
            List<StudentPlacementResponse> placements, int totalPlacements
    ) {}
}