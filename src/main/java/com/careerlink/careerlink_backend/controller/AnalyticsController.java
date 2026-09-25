package com.careerlink.careerlink_backend.controller;

import com.careerlink.careerlink_backend.dto.response.ApiResponse;
import com.careerlink.careerlink_backend.dto.response.analytics.*;
import com.careerlink.careerlink_backend.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/student/dashboard")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentAnalyticsResponse>> studentDashboard(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getStudentAnalytics(auth)));
    }

    @GetMapping("/recruiter/dashboard")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<RecruiterAnalyticsResponse>> recruiterDashboard(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getRecruiterAnalytics(auth)));
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminAnalyticsResponse>> adminDashboard() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getAdminAnalytics()));
    }

    @GetMapping("/admin/advanced")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdvancedAnalyticsResponse>> advancedAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String company) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getAdvancedAnalytics(startDate, endDate, branch, company)));
    }

    @GetMapping("/admin/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> exportData(
            @RequestParam String type,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String company) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.exportData(type, branch, company)));
    }
}
