package com.careerlink.careerlink_backend.dto.response;

public record AdminDashboardStatsResponse(
        StudentStats students,
        RecruiterStats recruiters,
        AlumniStats alumni,
        PackageStats packages,
        long pendingActionsTotal
) {
    public record StudentStats(long total, long placed, long unplaced, double placementPercentage) {}
    public record RecruiterStats(long total, long pending, long approved, long rejected) {}
    public record AlumniStats(long total, long pending, long approved, long rejected) {}
    public record PackageStats(Double average, Double highest, Double lowest) {}
}
