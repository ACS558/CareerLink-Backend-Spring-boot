package com.careerlink.careerlink_backend.dto.response.analytics;

import java.time.LocalDateTime;
import java.util.List;

public record StudentAnalyticsResponse(
        long totalApplications, long appliedCount, long shortlistedCount,
        long rejectedCount, long selectedCount, double avgAtsScore, int profileCompletion,
        List<RecentApplication> recentApplications, List<TrendPoint> applicationTrend
) {
    public record RecentApplication(String jobTitle, String company, String status, LocalDateTime appliedAt, double atsScore) {}
}
