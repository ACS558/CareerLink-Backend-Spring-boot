package com.careerlink.careerlink_backend.dto.gemini;

import java.util.List;

public record GeminiRequest(List<GeminiContent> contents) {

    public record GeminiContent(List<GeminiPart> parts) {}
    public record GeminiPart(String text) {}

    public static GeminiRequest of(String prompt) {
        return new GeminiRequest(List.of(new GeminiContent(List.of(new GeminiPart(prompt)))));
    }
}
