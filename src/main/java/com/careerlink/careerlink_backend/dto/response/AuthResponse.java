package com.careerlink.careerlink_backend.dto.response;

import java.time.LocalDateTime;

public record AuthResponse(
        String token,
        Long userId,
        String email,
        String role,
        String name,
        LocalDateTime previousLastLogin,
        String roleLevel
) {}
