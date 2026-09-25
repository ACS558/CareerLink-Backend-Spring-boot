package com.careerlink.careerlink_backend.dto.response.analytics;

import java.time.LocalDateTime;
import java.util.List;

public record AdminAnalyticsResponse(
        UserStats users, JobStats jobs, ApplicationStats applications, PlacementStats placements,
        RecentActivities recentActivities, Charts charts
) {
    public record UserStats(long total, long students, long recruiters, long active) {}
    public record JobStats(long total, long approved, long pending, long active) {}
    public record ApplicationStats(long total, long applied, long shortlisted, long selected, long rejected) {}
    public record PlacementStats(long placed, double percentage, double avgPackage, double highestPackage) {}
    public record RecentJob(String title, String company, String status, LocalDateTime date) {}
    public record RecentApplication(String student, String job, String status, LocalDateTime date) {}
    public record RecentActivities(List<RecentJob> jobs, List<RecentApplication> applications) {}
    public record StatusCount(String name, long value) {}
    public record BranchCount(String branch, long count) {}
    public record Charts(List<TrendPoint> applicationTrend, List<StatusCount> statusDistribution, List<BranchCount> branchDistribution) {}
}
