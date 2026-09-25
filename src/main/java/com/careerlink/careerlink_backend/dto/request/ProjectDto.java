package com.careerlink.careerlink_backend.dto.request;

import java.util.List;

public record ProjectDto(
        String title,
        String description,
        List<String> technologies,
        String liveLink,
        String githubLink
) {}
