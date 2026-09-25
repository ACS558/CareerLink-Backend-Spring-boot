package com.careerlink.careerlink_backend.dto.response.analytics;

import java.util.List;

public record AdvancedAnalyticsResponse(Overview overview, List<BranchAnalytics> branchWise,
                                        List<CompanyAnalytics> companyWise, List<CgpaAnalytics> cgpaAnalysis,
                                        List<SkillDemand> topSkills) {
    public record Overview(long totalApplications, long totalSelected, double overallSuccessRate) {}
    public record BranchAnalytics(String branch, long totalApplications, long selected, double placementRate, double avgAtsScore) {}
    public record CompanyAnalytics(String company, long totalApplications, long hired, long jobsPosted) {}
    public record CgpaAnalytics(String cgpaRange, long applications, long selected, double successRate) {}
    public record SkillDemand(String skill, long demand) {}
}
