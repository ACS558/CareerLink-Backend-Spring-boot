package com.careerlink.careerlink_backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OtpException extends RuntimeException {
    private final HttpStatus status;
    public OtpException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
