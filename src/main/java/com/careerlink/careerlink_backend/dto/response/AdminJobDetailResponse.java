package com.careerlink.careerlink_backend.dto.response;

import java.util.List;

public record AdminJobDetailResponse(
        JobResponse job,
        List<ApplicationResponse> applications,
        JobStats stats
) {
    public record JobStats(long total, long pending, long shortlisted, long selected, long rejected) {}
}
