package com.careerlink.careerlink_backend.mapper;

import com.careerlink.careerlink_backend.dto.response.PostDocumentResponse;
import com.careerlink.careerlink_backend.dto.response.PostImageResponse;
import com.careerlink.careerlink_backend.dto.response.PostResponse;
import com.careerlink.careerlink_backend.entity.Job;
import com.careerlink.careerlink_backend.entity.Post;
import com.careerlink.careerlink_backend.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PostMapper {

private final JobRepository jobRepository;

    public PostResponse toResponse(Post post, boolean hasViewed) {
        List<PostImageResponse> images = post.getImages().stream()
                .map(img -> new PostImageResponse(img.getId(), img.getUrl()))
                .toList();

        List<PostDocumentResponse> documents = post.getDocuments().stream()
                .map(doc -> new PostDocumentResponse(doc.getId(), doc.getUrl(), doc.getFileName(), doc.getFileType(), doc.getFileSize()))
                .toList();

        String linkedJobTitle = null;
        String linkedJobLocation = null;
        String linkedJobType = null;

        if (post.getLinkedJobId() != null) {
            Optional<Job> jobOpt = jobRepository.findById(post.getLinkedJobId());
            if (jobOpt.isPresent()) {
                Job job = jobOpt.get();
                linkedJobTitle = job.getTitle();
                linkedJobLocation = job.getLocation();
                linkedJobType = job.getJobType().name();
            }
        }

        return new PostResponse(
                post.getId(),
                post.getAuthorId(),
                post.getAuthorRole().name(),
                post.getAuthorName(),
                post.getAuthorPhotoUrl(),
                post.getContentType().name(),
                post.getTextContent(),
                images,
                documents,
                post.getLinkedJobId(),
                linkedJobTitle,
                linkedJobLocation,
                linkedJobType,
                post.isJobPost(),
                post.isPinned(),
                post.getViewCount(),
                hasViewed,
                post.getCreatedAt()
        );
    }
}
