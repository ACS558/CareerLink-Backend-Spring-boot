package com.careerlink.careerlink_backend.controller;

import com.careerlink.careerlink_backend.dto.request.ApplicationRequest;
import com.careerlink.careerlink_backend.dto.request.ApplicationStatusUpdateRequest;
import com.careerlink.careerlink_backend.dto.request.AutoShortlistRequest;
import com.careerlink.careerlink_backend.dto.request.BulkStatusUpdateRequest;
import com.careerlink.careerlink_backend.dto.response.ApiResponse;
import com.careerlink.careerlink_backend.dto.response.ApplicationResponse;
import com.careerlink.careerlink_backend.service.ApplicationService;
import com.careerlink.careerlink_backend.service.ExcelExportService;
import com.careerlink.careerlink_backend.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ExcelExportService excelExportService;
    private final JobService jobService;

    @GetMapping("/applications/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getApplicationById(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getApplicationById(auth, id)));
    }

    @DeleteMapping("/applications/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> withdrawApplication(Authentication auth, @PathVariable Long id) {
        applicationService.withdrawApplication(auth, id);
        return ResponseEntity.ok(ApiResponse.success("Application withdrawn successfully", null));
    }

    @PostMapping("/jobs/{jobId}/calculate-scores")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<String>> recalculateScores(Authentication auth, @PathVariable Long jobId) {
        int count = applicationService.recalculateAtsScores(auth, jobId);
        return ResponseEntity.ok(ApiResponse.success("ATS scores recalculated", count + " applications processed"));
    }

    @PostMapping("/jobs/apply/{jobId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> apply(
            Authentication auth, @PathVariable Long jobId,
            @RequestBody(required = false) ApplicationRequest req) {
        ApplicationResponse response = applicationService.applyForJob(auth, jobId, req);
        return ResponseEntity.ok(ApiResponse.success("Application submitted successfully", response));
    }

    @GetMapping("/jobs/my-applications")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> myApplications(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getMyApplications(auth)));
    }

    @GetMapping("/jobs/{jobId}/applications")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> applicationsForJob(
            Authentication auth, @PathVariable Long jobId) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getApplicationsForJob(auth, jobId)));
    }

    @PutMapping("/applications/{id}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateStatus(
            Authentication auth, @PathVariable Long id, @Valid @RequestBody ApplicationStatusUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", applicationService.updateStatus(auth, id, req)));
    }

    @PostMapping("/applications/bulk-update")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<String>> bulkUpdate(
            Authentication auth, @Valid @RequestBody BulkStatusUpdateRequest req) {
        int count = applicationService.bulkUpdateStatus(auth, req);
        return ResponseEntity.ok(ApiResponse.success("Bulk update completed", count + " applications updated"));
    }

    @PostMapping("/jobs/{jobId}/auto-shortlist")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<String>> autoShortlist(
            Authentication auth, @PathVariable Long jobId, @Valid @RequestBody AutoShortlistRequest req) {
        int count = applicationService.autoShortlist(auth, jobId, req);
        return ResponseEntity.ok(ApiResponse.success("Auto-shortlist completed", count + " candidates shortlisted"));
    }

    @GetMapping("/jobs/{jobId}/export")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<byte[]> exportApplicants(Authentication auth, @PathVariable Long jobId, @RequestParam(required = false) String status) {
        applicationServiceOwnedJobCheck(auth, jobId);

        byte[] data = excelExportService.exportApplicantsForJob(jobId, status);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=applicants_job_" + jobId + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    private void applicationServiceOwnedJobCheck(Authentication auth, Long jobId) {
        var recruiter = jobService.getCurrentRecruiter(auth);
        var job = jobService.getJobEntity(jobId);
        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new com.careerlink.careerlink_backend.exception.UnauthorizedException("This job does not belong to you");
        }
    }
}
