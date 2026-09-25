package com.careerlink.careerlink_backend.controller;

import com.careerlink.careerlink_backend.dto.response.ApiResponse;
import com.careerlink.careerlink_backend.dto.response.ReferralResponse;
import com.careerlink.careerlink_backend.service.ReferralService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/referrals")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'ALUMNI')")
public class ReferralController {

    private final ReferralService referralService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReferralResponse>>> browse(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(referralService.getApprovedReferrals(PageRequest.of(page, size))));
    }
}