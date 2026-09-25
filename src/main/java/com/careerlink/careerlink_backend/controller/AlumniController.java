package com.careerlink.careerlink_backend.controller;

import com.careerlink.careerlink_backend.dto.request.AlumniProfileUpdateRequest;
import com.careerlink.careerlink_backend.dto.request.ReferralRequest;
import com.careerlink.careerlink_backend.dto.response.ApiResponse;
import com.careerlink.careerlink_backend.dto.response.AlumniResponse;
import com.careerlink.careerlink_backend.dto.response.ReferralResponse;
import com.careerlink.careerlink_backend.service.AlumniService;
import com.careerlink.careerlink_backend.service.ReferralService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/alumni")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ALUMNI')")
public class AlumniController {

    private final AlumniService alumniService;
    private final ReferralService referralService;

    @DeleteMapping("/referrals/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReferral(Authentication auth, @PathVariable Long id) {
        referralService.deleteReferral(auth, id);
        return ResponseEntity.ok(ApiResponse.success("Referral deleted", null));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<AlumniResponse>> getProfile(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(alumniService.getProfile(auth)));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<AlumniResponse>> updateProfile(
            Authentication auth, @Valid @RequestBody AlumniProfileUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated", alumniService.updateProfile(auth, req)));
    }

    @PostMapping("/referrals")
    public ResponseEntity<ApiResponse<ReferralResponse>> submitReferral(
            Authentication auth, @Valid @RequestBody ReferralRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Referral submitted for admin approval", referralService.submitReferral(auth, req)));
    }

    @GetMapping("/referrals")
    public ResponseEntity<ApiResponse<List<ReferralResponse>>> myReferrals(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(referralService.getMyReferrals(auth)));
    }

    @PostMapping(value = "/profile-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AlumniResponse>> uploadProfilePhoto(
            Authentication auth, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("Profile photo uploaded successfully", alumniService.uploadProfilePhoto(auth, file)));
    }
}
