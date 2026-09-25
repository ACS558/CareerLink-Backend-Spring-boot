package com.careerlink.careerlink_backend.dto.response;

import java.time.LocalDateTime;

public record ExtensionRequestResponse(
        Long id,
        Long studentId,
        String registrationNumber,
        String name,
        String email,
        String reason,
        String status,
        LocalDateTime requestedAt,
        LocalDateTime reviewedAt,
        String accountStatus,
        LocalDateTime expiryDate
) {}
