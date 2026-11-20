package com.careerlink.careerlink_backend.service;

import com.careerlink.careerlink_backend.exception.OtpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
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
        post(to, "Reset your CareerLink password", html).subscribe(
                r -> log.info("Password reset email sent"),
                e -> log.error("Password reset email failed: {}", describe(e)));
    }

    public void sendOtpEmail(String to, String code, int minutes) {
        String html = "<p>Your CareerLink verification code is:</p>"
                + "<h2 style=\"letter-spacing:4px\">" + code + "</h2>"
                + "<p>It expires in " + minutes + " minutes. If you didn't request it, ignore this email.</p>";
        try {
            post(to, "Your CareerLink verification code", html).block(Duration.ofSeconds(10));
            log.info("OTP email sent");
        } catch (Exception e) {
            log.error("OTP email failed: {}", describe(e));
            throw new OtpException("Could not send the email. Please try again.", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private Mono<ResponseEntity<Void>> post(String to, String subject, String html) {
        Map<String, Object> body = Map.of(
                "sender", Map.of("name", fromName, "email", fromEmail),
                "to", List.of(Map.of("email", to)),
                "subject", subject,
                "htmlContent", html);
        return webClient.post().uri("/v3/smtp/email")
                .header("api-key", apiKey)
                .bodyValue(body)
                .retrieve().toBodilessEntity();
    }

    private String describe(Throwable e) {
        if (e instanceof WebClientResponseException w) {
            return "Brevo " + w.getStatusCode() + ": " + w.getResponseBodyAsString();
        }
        return e.getMessage();
    }
}
