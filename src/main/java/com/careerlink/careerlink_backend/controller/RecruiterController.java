package com.careerlink.careerlink_backend.controller;

import com.careerlink.careerlink_backend.dto.request.RecruiterProfileUpdateRequest;
import com.careerlink.careerlink_backend.dto.response.ApiResponse;
import com.careerlink.careerlink_backend.dto.response.RecruiterResponse;
import com.careerlink.careerlink_backend.service.RecruiterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/recruiter")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RECRUITER')")
public class RecruiterController {

    private final RecruiterService recruiterService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<RecruiterResponse>> getProfile(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(recruiterService.getProfile(auth)));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<RecruiterResponse>> updateProfile(
            Authentication auth, @Valid @RequestBody RecruiterProfileUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated", recruiterService.updateProfile(auth, req)));
    }

    @PostMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<RecruiterResponse>> uploadLogo(
            Authentication auth, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("Logo uploaded successfully", recruiterService.uploadLogo(auth, file)));
    }
}