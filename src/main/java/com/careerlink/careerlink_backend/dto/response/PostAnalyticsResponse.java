package com.careerlink.careerlink_backend.dto.response;

import java.time.LocalDateTime;

public record PostAnalyticsResponse(Long postId, long totalViewCount, long uniqueViewerCount, LocalDateTime createdAt) {}
