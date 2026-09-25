package com.careerlink.careerlink_backend.dto.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AtsGeminiResult(
        Double atsScore,
        List<String> strengthsList,
        List<String> weaknessesList,
        String overallSummary
) {}