package com.careerlink.careerlink_backend.controller;

import com.careerlink.careerlink_backend.dto.request.ExtensionRequestSubmitRequest;
import com.careerlink.careerlink_backend.dto.request.StudentProfileUpdateRequest;
import com.careerlink.careerlink_backend.dto.response.*;
import com.careerlink.careerlink_backend.service.StudentService;
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
@RequestMapping("/api/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    private final StudentService studentService;

    @DeleteMapping("/resume")
    public ResponseEntity<ApiResponse<Void>> deleteResume(Authentication auth) {
        studentService.deleteResume(auth);
        return ResponseEntity.ok(ApiResponse.success("Resume deleted successfully", null));
    }

    @DeleteMapping("/profile-photo")
    public ResponseEntity<ApiResponse<Void>> deleteProfilePhoto(Authentication auth) {
        studentService.deleteProfilePhoto(auth);
        return ResponseEntity.ok(ApiResponse.success("Photo deleted successfully", null));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<StudentDashboardResponse>> dashboard(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(studentService.getDashboard(auth)));
    }

    @GetMapping("/profile/completion")
    public ResponseEntity<ApiResponse<ProfileCompletionResponse>> profileCompletion(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(studentService.getProfileCompletion(auth)));
    }

    @GetMapping("/placements")
    public ResponseEntity<ApiResponse<List<StudentPlacementResponse>>> myPlacements(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(studentService.getMyPlacements(auth)));
    }

    @PostMapping("/request-extension")
    public ResponseEntity<ApiResponse<Void>> requestExtension(
            Authentication auth, @Valid @RequestBody ExtensionRequestSubmitRequest req) {
        studentService.requestExtension(auth, req);
        return ResponseEntity.ok(ApiResponse.success("Extension request submitted successfully", null));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<StudentResponse>> getProfile(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(studentService.getProfile(auth)));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<StudentResponse>> updateProfile(
            Authentication auth, @Valid @RequestBody StudentProfileUpdateRequest req) {
        StudentResponse response = studentService.updateProfile(auth, req);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @PostMapping(value = "/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<StudentResponse>> uploadResume(
            Authentication auth, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("Resume uploaded and parsed successfully", studentService.uploadResume(auth, file)));
    }

    @PostMapping(value = "/profile-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<StudentResponse>> uploadProfilePhoto(
            Authentication auth, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("Profile photo uploaded successfully", studentService.uploadProfilePhoto(auth, file)));
    }
}
