package com.careerlink.careerlink_backend.security;

import com.careerlink.careerlink_backend.exception.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(), HttpStatus.FORBIDDEN.value(), "Forbidden",
                "You do not have permission to access this resource", request.getRequestURI()
        );

        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}