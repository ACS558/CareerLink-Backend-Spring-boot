package com.careerlink.careerlink_backend.controller;

import com.careerlink.careerlink_backend.dto.request.JobCreateRequest;
import com.careerlink.careerlink_backend.dto.request.JobUpdateRequest;
import com.careerlink.careerlink_backend.dto.response.ApiResponse;
import com.careerlink.careerlink_backend.dto.response.JobResponse;
import com.careerlink.careerlink_backend.service.JobService;
import com.careerlink.careerlink_backend.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final StudentService studentService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponse>> getJobById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(jobService.getJobById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(
            Authentication auth, @PathVariable Long id, @RequestBody JobUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Job updated — awaiting admin approval", jobService.updateJob(auth, id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<Void>> deleteJob(Authentication auth, @PathVariable Long id) {
        jobService.deleteJob(auth, id);
        return ResponseEntity.ok(ApiResponse.success("Job deleted successfully", null));
    }

    @PostMapping
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<JobResponse>> createJob(
            Authentication auth, @Valid @RequestBody JobCreateRequest req) {
        JobResponse response = jobService.createJob(auth, req);
        return ResponseEntity.ok(ApiResponse.success("Job submitted for admin approval", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> getEligibleJobs(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var student = studentService.getCurrentStudent(auth);
        Page<JobResponse> jobs = jobService.getEligibleJobsForStudent(student, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @GetMapping("/my-postings")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<List<JobResponse>>> getMyPostings(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(jobService.getJobsByRecruiter(auth)));
    }
}
