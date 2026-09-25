package com.careerlink.careerlink_backend.exception;

import java.time.LocalDateTime;

public record ErrorResponse(LocalDateTime timestamp,
                            int status,
                            String error,
                            String message,
                            String path) {
}
