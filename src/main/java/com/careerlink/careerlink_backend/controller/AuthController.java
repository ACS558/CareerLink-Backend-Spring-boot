package com.careerlink.careerlink_backend.controller;

import com.careerlink.careerlink_backend.dto.request.AlumniRegisterRequest;
import com.careerlink.careerlink_backend.dto.request.LoginRequest;
import com.careerlink.careerlink_backend.dto.request.RecruiterRegisterRequest;
import com.careerlink.careerlink_backend.dto.request.StudentRegisterRequest;
import com.careerlink.careerlink_backend.dto.response.ApiResponse;
import com.careerlink.careerlink_backend.dto.response.AuthResponse;
import com.careerlink.careerlink_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
}