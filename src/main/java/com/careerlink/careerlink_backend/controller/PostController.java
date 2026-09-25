package com.careerlink.careerlink_backend.controller;

import com.careerlink.careerlink_backend.dto.response.ApiResponse;
import com.careerlink.careerlink_backend.dto.response.PostAnalyticsResponse;
import com.careerlink.careerlink_backend.dto.response.PostResponse;
import com.careerlink.careerlink_backend.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> getPostById(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(postService.getPostById(auth, id)));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER', 'ALUMNI')")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            Authentication auth, @PathVariable Long id,
            @RequestParam(value = "textContent", required = false) String textContent,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents) {
        return ResponseEntity.ok(ApiResponse.success("Post updated", postService.updatePost(auth, id, textContent, images, documents)));
    }

    @GetMapping("/analytics/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER', 'ALUMNI')")
    public ResponseEntity<ApiResponse<PostAnalyticsResponse>> analytics(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(postService.getPostAnalytics(auth, id)));
    }

    @DeleteMapping("/{id}/image/{imageId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER', 'ALUMNI')")
    public ResponseEntity<ApiResponse<PostResponse>> removeImage(
            Authentication auth, @PathVariable Long id, @PathVariable Long imageId) {
        return ResponseEntity.ok(ApiResponse.success("Image removed", postService.removeImage(auth, id, imageId)));
    }

    @DeleteMapping("/{id}/document/{documentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER', 'ALUMNI')")
    public ResponseEntity<ApiResponse<PostResponse>> removeDocument(
            Authentication auth, @PathVariable Long id, @PathVariable Long documentId) {
        return ResponseEntity.ok(ApiResponse.success("Document removed", postService.removeDocument(auth, id, documentId)));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER', 'ALUMNI')")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            Authentication auth,
            @RequestParam("textContent") String textContent,
            @RequestParam(value = "linkedJobId", required = false) Long linkedJobId,
            @RequestParam(value = "isJobPost", required = false) Boolean isJobPost,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents) {

        PostResponse response = postService.createPost(auth, textContent, linkedJobId, isJobPost, images, documents);
        return ResponseEntity.ok(ApiResponse.success("Post created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getFeed(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(postService.getFeed(auth, PageRequest.of(page, size))));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER', 'ALUMNI')")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getMyPosts(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(postService.getMyPosts(auth)));
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<ApiResponse<Void>> recordView(Authentication auth, @PathVariable Long id) {
        postService.recordView(auth, id);
        return ResponseEntity.ok(ApiResponse.success("View recorded", null));
    }

    @PostMapping("/{id}/pin")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER', 'ALUMNI')")
    public ResponseEntity<ApiResponse<PostResponse>> pinPost(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Post pinned", postService.pinPost(auth, id)));
    }

    @PostMapping("/{id}/unpin")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER', 'ALUMNI')")
    public ResponseEntity<ApiResponse<PostResponse>> unpinPost(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Post unpinned", postService.unpinPost(auth, id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(Authentication auth, @PathVariable Long id) {
        postService.deletePost(auth, id);
        return ResponseEntity.ok(ApiResponse.success("Post deleted", null));
    }
}
