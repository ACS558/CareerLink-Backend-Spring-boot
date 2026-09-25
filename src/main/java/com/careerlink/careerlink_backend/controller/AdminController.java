package com.careerlink.careerlink_backend.controller;

import com.careerlink.careerlink_backend.dto.request.*;
import com.careerlink.careerlink_backend.dto.response.*;
import com.careerlink.careerlink_backend.service.AdminService;
import com.careerlink.careerlink_backend.service.ExcelExportService;
import com.careerlink.careerlink_backend.service.ExtensionRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final ExcelExportService excelExportService;
    private final ExtensionRequestService extensionRequestService;

    @GetMapping("/extension-requests")
    public ResponseEntity<ApiResponse<List<ExtensionRequestResponse>>> extensionRequests(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(extensionRequestService.listRequests(status)));
    }

    @GetMapping("/extension-requests/stats")
    public ResponseEntity<ApiResponse<ExtensionStatsResponse>> extensionRequestStats() {
        return ResponseEntity.ok(ApiResponse.success(extensionRequestService.getStats()));
    }

    @PostMapping("/extension-requests/{studentId}/{requestId}/review")
    public ResponseEntity<ApiResponse<Void>> reviewExtensionRequest(
            Authentication auth, @PathVariable Long studentId, @PathVariable Long requestId,
            @RequestBody ExtensionReviewRequest req) {
        extensionRequestService.reviewRequest(auth, studentId, requestId, req);
        return ResponseEntity.ok(ApiResponse.success("Request reviewed successfully", null));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<AdminProfileResponse>> getProfile(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getProfile(auth)));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<AdminProfileResponse>> updateProfile(
            Authentication auth, @RequestBody AdminProfileUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated", adminService.updateProfile(auth, req)));
    }

    @GetMapping("/recruiters")
    public ResponseEntity<ApiResponse<List<RecruiterResponse>>> allRecruiters(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String verificationStatus) {
        return ResponseEntity.ok(ApiResponse.success(adminService.listAllRecruiters(search, verificationStatus)));
    }

    @GetMapping("/alumni")
    public ResponseEntity<ApiResponse<List<AlumniResponse>>> allAlumni(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String verificationStatus,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) Integer graduationYear) {
        return ResponseEntity.ok(ApiResponse.success(adminService.listAllAlumni(search, verificationStatus, branch, graduationYear)));
    }

    @GetMapping("/students")
    public ResponseEntity<ApiResponse<Page<StudentResponse>>> allStudents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String placementStatus,
            @RequestParam(required = false) Integer graduationYear,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                adminService.listAllStudents(search, branch, placementStatus, graduationYear, PageRequest.of(page, size))));
    }

    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<List<JobResponse>>> allJobs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String approvalStatus) {
        return ResponseEntity.ok(ApiResponse.success(adminService.listAllJobs(search, approvalStatus)));
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<ApiResponse<AdminJobDetailResponse>> jobDetail(@PathVariable Long jobId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getJobDetail(jobId)));
    }

    @GetMapping("/jobs/{jobId}/export")
    public ResponseEntity<byte[]> exportJobApplicants(@PathVariable Long jobId, @RequestParam(required = false) String status) {
        byte[] data = excelExportService.exportApplicantsForJob(jobId, status);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=applicants_job_" + jobId + ".xlsx")
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @PutMapping("/applications/{id}/status")
    public ResponseEntity<ApiResponse<ApplicationResponse>> adminUpdateApplicationStatus(
            Authentication auth, @PathVariable Long id, @RequestBody ApplicationStatusUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", adminService.adminUpdateApplicationStatus(auth, id, req)));
    }

    @PatchMapping("/applications/bulk-update")
    public ResponseEntity<ApiResponse<String>> adminBulkUpdate(@RequestBody BulkStatusUpdateRequest req) {
        int count = adminService.adminBulkUpdateApplications(req);
        return ResponseEntity.ok(ApiResponse.success("Bulk update completed", count + " applications updated"));
    }

    @GetMapping("/referrals")
    public ResponseEntity<ApiResponse<List<ReferralResponse>>> allReferrals() {
        return ResponseEntity.ok(ApiResponse.success(adminService.listAllReferrals()));
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<AdminDashboardStatsResponse>> dashboardStats() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDashboardStats()));
    }


    @GetMapping("/recruiters/pending")
    public ResponseEntity<ApiResponse<List<RecruiterResponse>>> pendingRecruiters() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getPendingRecruiters()));
    }

    @PostMapping("/recruiters/{id}/approve")
    public ResponseEntity<ApiResponse<RecruiterResponse>> approveRecruiter(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Recruiter approved", adminService.approveRecruiter(auth, id)));
    }

    @PostMapping("/recruiters/{id}/reject")
    public ResponseEntity<ApiResponse<RecruiterResponse>> rejectRecruiter(
            Authentication auth, @PathVariable Long id, @RequestBody RejectionRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Recruiter rejected", adminService.rejectRecruiter(auth, id, req.reason())));
    }

    @GetMapping("/alumni/pending")
    public ResponseEntity<ApiResponse<List<AlumniResponse>>> pendingAlumni() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getPendingAlumni()));
    }

    @PostMapping("/alumni/{id}/approve")
    public ResponseEntity<ApiResponse<AlumniResponse>> approveAlumni(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Alumni approved", adminService.approveAlumni(auth, id)));
    }

    @PostMapping("/alumni/{id}/reject")
    public ResponseEntity<ApiResponse<AlumniResponse>> rejectAlumni(
            Authentication auth, @PathVariable Long id, @RequestBody RejectionRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Alumni rejected", adminService.rejectAlumni(auth, id, req.reason())));
    }

    @GetMapping("/jobs/pending")
    public ResponseEntity<ApiResponse<List<JobResponse>>> pendingJobs() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getPendingJobs()));
    }

    @PostMapping("/jobs/{id}/approve")
    public ResponseEntity<ApiResponse<JobResponse>> approveJob(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Job approved", adminService.approveJob(auth, id)));
    }

    @PostMapping("/jobs/{id}/reject")
    public ResponseEntity<ApiResponse<JobResponse>> rejectJob(
            Authentication auth, @PathVariable Long id, @RequestBody RejectionRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Job rejected", adminService.rejectJob(auth, id, req.reason())));
    }

    @GetMapping("/referrals/pending")
    public ResponseEntity<ApiResponse<List<ReferralResponse>>> pendingReferrals() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getPendingReferrals()));
    }

    @PostMapping("/referrals/{id}/approve")
    public ResponseEntity<ApiResponse<ReferralResponse>> approveReferral(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Referral approved", adminService.approveReferral(auth, id)));
    }

    @PostMapping("/referrals/{id}/reject")
    public ResponseEntity<ApiResponse<ReferralResponse>> rejectReferral(
            Authentication auth, @PathVariable Long id, @RequestBody RejectionRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Referral rejected", adminService.rejectReferral(auth, id, req.reason())));
    }

    @PostMapping("/students/{id}/extend")
    public ResponseEntity<ApiResponse<Void>> extendStudent(@PathVariable Long id) {
        adminService.extendStudentAccount(id);
        return ResponseEntity.ok(ApiResponse.success("Student account extended", null));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Void>> createAdmin(Authentication auth, @Valid @RequestBody AdminCreateRequest req) {
        adminService.createAdmin(auth, req);
        return ResponseEntity.ok(ApiResponse.success("Admin account created", null));
    }

    @GetMapping("/reports/export")
    public ResponseEntity<byte[]> exportStudentsReport(
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String status) {

        byte[] data = excelExportService.exportStudentsReport(branch, status);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students_report.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @GetMapping("/admins")
    public ResponseEntity<ApiResponse<List<AdminListResponse>>> allAdmins() {
        return ResponseEntity.ok(ApiResponse.success(adminService.listAllAdmins()));
    }

    @DeleteMapping("/admins/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAdmin(Authentication auth, @PathVariable Long id) {
        adminService.deleteAdmin(auth, id);
        return ResponseEntity.ok(ApiResponse.success("Admin deleted successfully", null));
    }

    @GetMapping("/applications")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> allApplications(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) Long studentId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.listAllApplications(status, jobId, studentId)));
    }
}