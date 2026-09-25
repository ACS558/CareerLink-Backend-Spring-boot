package com.careerlink.careerlink_backend.dto.response;

import java.util.List;

public record AtsScoreResponse(
        Double score,
        List<String> strengths,
        List<String> weaknesses,
        String recommendation,
        String overallSummary
) {}
