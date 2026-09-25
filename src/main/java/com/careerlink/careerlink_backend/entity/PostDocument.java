package com.careerlink.careerlink_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "post_documents")
public class PostDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    private String url;
    private String publicId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private LocalDateTime uploadedAt;
}
