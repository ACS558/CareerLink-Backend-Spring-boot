package com.careerlink.careerlink_backend.controller;

import com.careerlink.careerlink_backend.dto.request.*;
import com.careerlink.careerlink_backend.dto.response.ApiResponse;
import com.careerlink.careerlink_backend.dto.response.AuthResponse;
import com.careerlink.careerlink_backend.service.AuthService;
import com.careerlink.careerlink_backend.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register/student")
    public ResponseEntity<ApiResponse<AuthResponse>> registerStudent(@Valid @RequestBody StudentRegisterRequest req) {
        AuthResponse response = authService.registerStudent(req);
        return ResponseEntity.ok(ApiResponse.success("Student registered successfully", response));
    }

    @PostMapping("/register/recruiter")
    public ResponseEntity<ApiResponse<AuthResponse>> registerRecruiter(@Valid @RequestBody RecruiterRegisterRequest req) {
        AuthResponse response = authService.registerRecruiter(req);
        return ResponseEntity.ok(ApiResponse.success("Recruiter registered — pending admin approval", response));
    }

    @PostMapping("/register/alumni")
    public ResponseEntity<ApiResponse<AuthResponse>> registerAlumni(@Valid @RequestBody AlumniRegisterRequest req) {
        AuthResponse response = authService.registerAlumni(req);
        return ResponseEntity.ok(ApiResponse.success("Alumni registered — pending admin approval", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        AuthResponse response = authService.login(req);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        passwordResetService.requestReset(req.identifier());
        return ResponseEntity.ok(ApiResponse.success("If an account exists, a reset link has been sent to its email", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        passwordResetService.resetPassword(req.token(), req.newPassword());
        return ResponseEntity.ok(ApiResponse.success("Password reset successful. Please log in.", null));
    }
}