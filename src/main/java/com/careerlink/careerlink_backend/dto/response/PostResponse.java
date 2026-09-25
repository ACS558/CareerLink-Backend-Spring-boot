package com.careerlink.careerlink_backend.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
        Long id,
        Long authorId,
        String authorRole,
        String authorName,
        String authorPhotoUrl,
        String contentType,
        String textContent,
        List<PostImageResponse> images,
        List<PostDocumentResponse> documents,
        Long linkedJobId,
        String linkedJobTitle,
        String linkedJobLocation,
        String linkedJobType,
        boolean isJobPost,
        boolean isPinned,
        long viewCount,
        boolean hasViewed,
        LocalDateTime createdAt
) {}
