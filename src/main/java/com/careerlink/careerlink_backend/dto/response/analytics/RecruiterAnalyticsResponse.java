package com.careerlink.careerlink_backend.dto.response.analytics;

import java.time.LocalDateTime;
import java.util.List;

public record RecruiterAnalyticsResponse(
        long totalJobs, long activeJobs, long pendingJobs, long rejectedJobs,
        long totalApplications, long newApplications, long shortlistedApplications, long selectedApplications,
        List<TopJob> topJobs, List<TrendPoint> applicationTrend,
        List<StatusCount> statusDistribution, List<RecentApplication> recentApplications
) {
    public record TopJob(String title, long applications, String status) {}
    public record StatusCount(String status, long count) {}
    public record RecentApplication(String studentName, String jobTitle, double atsScore, String status, LocalDateTime appliedAt) {}
}
