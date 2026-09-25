package com.careerlink.careerlink_backend.service;

import com.careerlink.careerlink_backend.dto.gemini.AtsGeminiResult;
import com.careerlink.careerlink_backend.dto.gemini.GeminiRequest;
import com.careerlink.careerlink_backend.dto.gemini.GeminiResponse;
import com.careerlink.careerlink_backend.entity.embeddable.AtsScore;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AtsService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api-url}")
    private String apiUrl;

    @Value("${gemini.api-key}")
    private String apiKey;

    public AtsService(WebClient geminiWebClient) {
        this.webClient = geminiWebClient;
    }

    public AtsScore calculateScore(String resumeText, String jobDescription, List<String> requiredSkills) {
        try {
            String prompt = buildPrompt(resumeText, jobDescription, requiredSkills);
            GeminiRequest request = GeminiRequest.of(prompt);

            GeminiResponse response = webClient.post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .timeout(Duration.ofSeconds(20))
                    .block();

            String rawText = response != null ? response.extractText() : null;
            if (rawText == null) {
                throw new IllegalStateException("Empty response from Gemini");
            }

            String cleaned = rawText.replaceAll("```json|```", "").trim();
            AtsGeminiResult result = objectMapper.readValue(cleaned, AtsGeminiResult.class);

            return toAtsScore(result);

        } catch (Exception e) {
            log.error("Gemini ATS scoring failed, falling back to keyword match: {}", e.getMessage());
            return fallbackScore(resumeText, requiredSkills);
        }
    }

    private String buildPrompt(String resumeText, String jobDescription, List<String> requiredSkills) {
        return """
                You are an expert ATS system. Analyze the compatibility between this resume and job description.

                RESUME:
                %s

                JOB DESCRIPTION:
                %s

                REQUIRED SKILLS:
                %s

                Respond ONLY with a valid JSON object in this exact format, no markdown, no preamble:
                {
                  "atsScore": <number between 0-100>,
                  "strengthsList": [<matched skills/strengths as strings>],
                  "weaknessesList": [<missing skills/gaps as strings>],
                  "overallSummary": "<brief 2-3 sentence summary>"
                }
                """.formatted(
                resumeText == null ? "" : resumeText,
                jobDescription == null ? "" : jobDescription,
                String.join(", ", requiredSkills)
        );
    }

    private AtsScore toAtsScore(AtsGeminiResult result) {
        double score = result.atsScore() != null ? result.atsScore() : 0.0;

        AtsScore atsScore = new AtsScore();
        atsScore.setScore(Math.round(score * 100.0) / 100.0);
        atsScore.setStrengths(result.strengthsList() != null ? result.strengthsList() : new ArrayList<>());
        atsScore.setWeaknesses(result.weaknessesList() != null ? result.weaknessesList() : new ArrayList<>());
        atsScore.setRecommendation(recommendationFor(score));
        atsScore.setOverallSummary(result.overallSummary());
        atsScore.setCalculatedAt(LocalDateTime.now());
        return atsScore;
    }

    private String recommendationFor(double score) {
        if (score >= 80) return "Highly recommended";
        if (score >= 60) return "Recommended";
        if (score >= 40) return "Maybe";
        return "Not recommended";
    }

    // Used only if the Gemini call fails — keeps the apply-flow resilient instead of throwing 500s.
    private AtsScore fallbackScore(String resumeText, List<String> requiredSkills) {
        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        String normalizedResume = resumeText == null ? "" : resumeText.toLowerCase();

        for (String skill : requiredSkills) {
            if (normalizedResume.contains(skill.toLowerCase())) strengths.add(skill);
            else weaknesses.add(skill);
        }

        double score = requiredSkills.isEmpty() ? 50.0 : (strengths.size() * 100.0) / requiredSkills.size();

        AtsScore atsScore = new AtsScore();
        atsScore.setScore(Math.round(score * 100.0) / 100.0);
        atsScore.setStrengths(strengths);
        atsScore.setWeaknesses(weaknesses);
        atsScore.setRecommendation(recommendationFor(score));
        atsScore.setOverallSummary("Automated fallback scoring (AI service temporarily unavailable).");
        atsScore.setCalculatedAt(LocalDateTime.now());
        return atsScore;
    }
}
