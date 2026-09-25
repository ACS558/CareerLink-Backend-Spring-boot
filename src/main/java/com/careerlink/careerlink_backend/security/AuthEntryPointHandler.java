package com.careerlink.careerlink_backend.security;

import com.careerlink.careerlink_backend.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class AuthEntryPointHandler implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(), "Unauthorized",
                "Authentication required or token is invalid/expired", request.getRequestURI()
        );

        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}