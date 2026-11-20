package com.careerlink.careerlink_backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EmailService {
    private final WebClient webClient = WebClient.builder().baseUrl("https://api.brevo.com").build();

    @Value("${mail.api-key}") private String apiKey;
    @Value("${mail.from-email}") private String fromEmail;
    @Value("${mail.from-name:CareerLink}") private String fromName;

    public void sendPasswordResetLink(String to, String link, int minutes) {
        String html = "<p>We received a request to reset your CareerLink password.</p>"
                + "<p><a href=\"" + link + "\">Reset your password</a></p>"
                + "<p>This link expires in " + minutes + " minutes. "
                + "If you didn't request it, you can ignore this email.</p>";

        Map<String, Object> body = Map.of(
                "sender", Map.of("name", fromName, "email", fromEmail),
                "to", List.of(Map.of("email", to)),
                "subject", "Reset your CareerLink password",
                "htmlContent", html);

        webClient.post().uri("/v3/smtp/email")
                .header("api-key", apiKey)
                .bodyValue(body)
                .retrieve().toBodilessEntity()
                .subscribe(r -> log.info("Password reset email sent"),
                        e -> log.error("Password reset email failed: {}", e.getMessage()));
    }
}
