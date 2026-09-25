package com.careerlink.careerlink_backend.service;

import com.careerlink.careerlink_backend.dto.response.PostResponse;
import com.careerlink.careerlink_backend.dto.response.PostAnalyticsResponse;
import com.careerlink.careerlink_backend.entity.*;
import com.careerlink.careerlink_backend.entity.enums.AuthorRole;
import com.careerlink.careerlink_backend.entity.enums.ContentType;
import com.careerlink.careerlink_backend.entity.enums.NotificationType;
import com.careerlink.careerlink_backend.entity.enums.Role;
import com.careerlink.careerlink_backend.entity.enums.VerificationStatus;
import com.careerlink.careerlink_backend.exception.ResourceNotFoundException;
import com.careerlink.careerlink_backend.exception.UnauthorizedException;
import com.careerlink.careerlink_backend.mapper.PostMapper;
import com.careerlink.careerlink_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostViewRepository postViewRepository;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final RecruiterRepository recruiterRepository;
    private final AlumniRepository alumniRepository;
    private final CloudinaryService cloudinaryService;
    private final NotificationService notificationService;
    private final PostMapper postMapper;

    @Transactional
    public PostResponse createPost(Authentication auth, String textContent, Long linkedJobId, Boolean isJobPost,
                                   List<MultipartFile> images, List<MultipartFile> documents) {

        User user = currentUser(auth);
        AuthorInfo authorInfo = resolveAuthor(user);

        Post post = new Post();
        post.setAuthorId(user.getId());
        post.setAuthorRole(authorInfo.role());
        post.setAuthorName(authorInfo.name());
        post.setAuthorPhotoUrl(authorInfo.photoUrl());
        post.setTextContent(textContent);
        post.setLinkedJobId(linkedJobId);
        post.setJobPost(isJobPost != null && isJobPost);

        boolean hasImages = images != null && !images.isEmpty();
        boolean hasDocs = documents != null && !documents.isEmpty();

        if (hasImages && hasDocs) post.setContentType(ContentType.MIXED);
        else if (hasImages) post.setContentType(ContentType.TEXT_IMAGE);
        else if (hasDocs) post.setContentType(ContentType.TEXT_DOCUMENT);
        else post.setContentType(ContentType.TEXT);

        Post saved = postRepository.save(post);

        if (hasImages) {
            for (MultipartFile file : images) {
                var uploaded = cloudinaryService.uploadFile(file, "careerlink/post-images", "image");
                PostImage img = new PostImage();
                img.setPost(saved);
                img.setUrl(uploaded.getUrl());
                img.setPublicId(uploaded.getPublicId());
                img.setSize(file.getSize());
                img.setUploadedAt(LocalDateTime.now());
                saved.getImages().add(img);
            }
        }

        if (hasDocs) {
            for (MultipartFile file : documents) {
                var uploaded = cloudinaryService.uploadFile(file, "careerlink/post-documents", "raw");
                PostDocument doc = new PostDocument();
                doc.setPost(saved);
                doc.setUrl(uploaded.getUrl());
                doc.setPublicId(uploaded.getPublicId());
                doc.setFileName(file.getOriginalFilename());
                doc.setFileType(file.getContentType());
                doc.setFileSize(file.getSize());
                doc.setUploadedAt(LocalDateTime.now());
                saved.getDocuments().add(doc);
            }
        }

        Post finalSaved = postRepository.save(saved);

        notificationService.notifyAllStudents(
                NotificationType.NEW_POST,
                "New Update from " + authorInfo.name(),
                truncate(textContent, 100),
                "/student/feed"
        );

        return postMapper.toResponse(finalSaved, false);
    }

    public Page<PostResponse> getFeed(Authentication auth, Pageable pageable) {
        User user = currentUser(auth);
        return postRepository.findByIsDeletedFalseOrderByIsPinnedDescCreatedAtDesc(pageable)
                .map(post -> postMapper.toResponse(post, hasUserViewed(post.getId(), user.getId())));
    }

    public List<PostResponse> getMyPosts(Authentication auth) {
        User user = currentUser(auth);
        Page<Post> page = postRepository.findByAuthorIdAndIsDeletedFalseOrderByCreatedAtDesc(user.getId(), Pageable.unpaged());
        return page.getContent().stream()
                .map(post -> postMapper.toResponse(post, false))
                .toList();
    }

    @Transactional
    public void recordView(Authentication auth, Long postId) {
        User user = currentUser(auth);
        Post post = getPostEntity(postId);

        if (postViewRepository.existsByPostIdAndViewedById(postId, user.getId())) {
            return; // already counted, no-op
        }

        PostView view = new PostView();
        view.setPost(post);
        view.setViewedBy(user);
        view.setViewerRole(user.getRole());
        postViewRepository.save(view);

        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
    }

    @Transactional
    public PostResponse pinPost(Authentication auth, Long postId) {
        User user = currentUser(auth);
        Post post = getPostEntity(postId);

        if (post.isPinned()) {
            throw new UnauthorizedException("Post is already pinned");
        }

        boolean isAuthor = post.getAuthorId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isAuthor && !isAdmin) {
            throw new UnauthorizedException("You can only pin your own posts");
        }

        if (!isAdmin) {
            int limit = user.getRole() == Role.RECRUITER ? 3 : 2; // alumni or recruiter
            long currentPinnedCount = postRepository.findByAuthorIdAndIsDeletedFalseOrderByCreatedAtDesc(user.getId(), Pageable.unpaged())
                    .getContent().stream().filter(Post::isPinned).count();

            if (currentPinnedCount >= limit) {
                postRepository.findByAuthorIdAndIsDeletedFalseOrderByCreatedAtDesc(user.getId(), Pageable.unpaged())
                        .getContent().stream()
                        .filter(Post::isPinned)
                        .min((a, b) -> a.getPinnedAt().compareTo(b.getPinnedAt()))
                        .ifPresent(oldest -> {
                            oldest.setPinned(false);
                            oldest.setPinnedBy(null);
                            oldest.setPinnedAt(null);
                            postRepository.save(oldest);
                        });
            }
        }

        post.setPinned(true);
        post.setPinnedAt(LocalDateTime.now());
        if (isAdmin) {
            post.setPinnedBy(currentAdmin(auth));
        }

        return postMapper.toResponse(postRepository.save(post), false);
    }

    @Transactional
    public PostResponse unpinPost(Authentication auth, Long postId) {
        User user = currentUser(auth);
        Post post = getPostEntity(postId);

        boolean isAuthor = post.getAuthorId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isAuthor && !isAdmin) {
            throw new UnauthorizedException("You can only unpin your own posts");
        }

        post.setPinned(false);
        post.setPinnedBy(null);
        post.setPinnedAt(null);

        return postMapper.toResponse(postRepository.save(post), false);
    }

    @Transactional
    public void deletePost(Authentication auth, Long postId) {
        User user = currentUser(auth);
        Post post = getPostEntity(postId);

        boolean isOwner = post.getAuthorId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new UnauthorizedException("You can only delete your own posts");
        }

        post.setDeleted(true);
        post.setDeletedBy(user);
        post.setDeletedAt(LocalDateTime.now());
        postRepository.save(post);
    }

    public Post getPostEntity(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
    }

    private boolean hasUserViewed(Long postId, Long userId) {
        return postViewRepository.existsByPostIdAndViewedById(postId, userId);
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Admin currentAdmin(Authentication auth) {
        User user = currentUser(auth);
        return adminRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Admin profile not found"));
    }

    private AuthorInfo resolveAuthor(User user) {
        return switch (user.getRole()) {
            case ADMIN -> {
                Admin admin = adminRepository.findByUserId(user.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Admin profile not found"));
                String name = admin.getPersonalInfo() != null
                        ? (safe(admin.getPersonalInfo().getFirstName()) + " " + safe(admin.getPersonalInfo().getLastName())).trim()
                        : "Admin";
                yield new AuthorInfo(AuthorRole.ADMIN, name, null);
            }
            case RECRUITER -> {
                Recruiter recruiter = recruiterRepository.findByUserId(user.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found"));
                if (recruiter.getVerificationStatus() != VerificationStatus.APPROVED) {
                    throw new UnauthorizedException("Your recruiter account is not yet approved by admin");
                }
                String name = recruiter.getCompanyInfo() != null ? recruiter.getCompanyInfo().getCompanyName() : "Recruiter";
                String photo = recruiter.getCompanyInfo() != null ? recruiter.getCompanyInfo().getCompanyLogoUrl() : null;
                yield new AuthorInfo(AuthorRole.RECRUITER, name, photo);
            }
            case ALUMNI -> {
                Alumni alumni = alumniRepository.findByUserId(user.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Alumni profile not found"));
                if (alumni.getVerificationStatus() != VerificationStatus.APPROVED) {
                    throw new UnauthorizedException("Your alumni account is not yet approved by admin");
                }
                String name = alumni.getPersonalInfo() != null
                        ? (safe(alumni.getPersonalInfo().getFirstName()) + " " + safe(alumni.getPersonalInfo().getLastName())).trim()
                        : alumni.getRegistrationNumber();
                String photo = alumni.getPersonalInfo() != null ? alumni.getPersonalInfo().getProfilePictureUrl() : null;
                yield new AuthorInfo(AuthorRole.ALUMNI, name, photo);
            }
            case STUDENT -> throw new UnauthorizedException("Students cannot create posts");
        };
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "...";
    }

    private record AuthorInfo(AuthorRole role, String name, String photoUrl) {}

    public PostResponse getPostById(Authentication auth, Long postId) {
        User user = currentUser(auth);
        Post post = getPostEntity(postId);

        if ((user.getRole() == Role.RECRUITER || user.getRole() == Role.ALUMNI)
                && !post.getAuthorId().equals(user.getId())) {
            throw new UnauthorizedException("You can only view your own posts");
        }
        return postMapper.toResponse(post, hasUserViewed(postId, user.getId()));
    }

    @Transactional
    public PostResponse updatePost(Authentication auth, Long postId, String textContent,
                                   List<MultipartFile> newImages, List<MultipartFile> newDocuments) {
        User user = currentUser(auth);
        Post post = getPostEntity(postId);

        if (!post.getAuthorId().equals(user.getId())) {
            throw new UnauthorizedException("You can only edit your own posts");
        }

        if (textContent != null) post.setTextContent(textContent);

        if (newImages != null && !newImages.isEmpty()) {
            for (MultipartFile file : newImages) {
                var uploaded = cloudinaryService.uploadFile(file, "careerlink/post-images", "image");
                PostImage img = new PostImage();
                img.setPost(post);
                img.setUrl(uploaded.getUrl());
                img.setPublicId(uploaded.getPublicId());
                img.setSize(file.getSize());
                img.setUploadedAt(LocalDateTime.now());
                post.getImages().add(img);
            }
        }

        if (newDocuments != null && !newDocuments.isEmpty()) {
            for (MultipartFile file : newDocuments) {
                var uploaded = cloudinaryService.uploadFile(file, "careerlink/post-documents", "raw");
                PostDocument doc = new PostDocument();
                doc.setPost(post);
                doc.setUrl(uploaded.getUrl());
                doc.setPublicId(uploaded.getPublicId());
                doc.setFileName(file.getOriginalFilename());
                doc.setFileType(file.getContentType());
                doc.setFileSize(file.getSize());
                doc.setUploadedAt(LocalDateTime.now());
                post.getDocuments().add(doc);
            }
        }

        boolean hasImages = !post.getImages().isEmpty();
        boolean hasDocs = !post.getDocuments().isEmpty();
        if (hasImages && hasDocs) post.setContentType(ContentType.MIXED);
        else if (hasImages) post.setContentType(ContentType.TEXT_IMAGE);
        else if (hasDocs) post.setContentType(ContentType.TEXT_DOCUMENT);
        else post.setContentType(ContentType.TEXT);

        return postMapper.toResponse(postRepository.save(post), false);
    }

    public PostAnalyticsResponse getPostAnalytics(Authentication auth, Long postId) {
        User user = currentUser(auth);
        Post post = getPostEntity(postId);

        boolean isAuthor = post.getAuthorId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isAuthor && !isAdmin) {
            throw new UnauthorizedException("Not authorized to view analytics for this post");
        }

        long uniqueViews = postViewRepository.countByPostId(postId);
        return new PostAnalyticsResponse(post.getId(), post.getViewCount(), uniqueViews, post.getCreatedAt());
    }

    @Transactional
    public PostResponse removeImage(Authentication auth, Long postId, Long imageId) {
        User user = currentUser(auth);
        Post post = getPostEntity(postId);

        if (!post.getAuthorId().equals(user.getId())) {
            throw new UnauthorizedException("You can only edit your own posts");
        }

        PostImage toRemove = post.getImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Image not found on this post"));

        cloudinaryService.deleteFile(toRemove.getPublicId(), "image");
        post.getImages().remove(toRemove);

        return postMapper.toResponse(postRepository.save(post), false);
    }

    @Transactional
    public PostResponse removeDocument(Authentication auth, Long postId, Long documentId) {
        User user = currentUser(auth);
        Post post = getPostEntity(postId);

        if (!post.getAuthorId().equals(user.getId())) {
            throw new UnauthorizedException("You can only edit your own posts");
        }

        PostDocument toRemove = post.getDocuments().stream()
                .filter(doc -> doc.getId().equals(documentId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Document not found on this post"));

        cloudinaryService.deleteFile(toRemove.getPublicId(), "raw");
        post.getDocuments().remove(toRemove);

        return postMapper.toResponse(postRepository.save(post), false);
    }
}
