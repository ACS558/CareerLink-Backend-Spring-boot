package com.careerlink.careerlink_backend.entity;

import com.careerlink.careerlink_backend.entity.enums.AuthorRole;
import com.careerlink.careerlink_backend.entity.enums.ContentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "posts")
public class Post extends BaseEntity {

    private Long authorId;

    @Enumerated(EnumType.STRING)
    private AuthorRole authorRole;

    private String authorName;
    private String authorPhotoUrl;

    @Enumerated(EnumType.STRING)
    private ContentType contentType = ContentType.TEXT;

    @Lob
    @Column(length = 5000)
    private String textContent;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostDocument> documents = new ArrayList<>();

    private Long linkedJobId;
    private boolean isJobPost = false;
    private boolean isPinned = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pinned_by")
    private Admin pinnedBy;

    private LocalDateTime pinnedAt;
    private Long viewCount = 0L;
    private boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private User deletedBy;

    private LocalDateTime deletedAt;
}
