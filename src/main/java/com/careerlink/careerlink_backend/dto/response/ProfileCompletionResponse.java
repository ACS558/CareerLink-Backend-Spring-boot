package com.careerlink.careerlink_backend.dto.response;

import java.util.Map;

public record ProfileCompletionResponse(
        int completionPercentage,
        boolean profileCompleted,
        Map<String, SectionStatus> breakdown,
        String message
) {
    public record SectionStatus(boolean completed, int weight) {}
}
