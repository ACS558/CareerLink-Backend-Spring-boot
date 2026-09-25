package com.careerlink.careerlink_backend.entity;

import com.careerlink.careerlink_backend.entity.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "post_views",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "viewed_by"})
)
public class PostView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viewed_by", nullable = false)
    private User viewedBy;

    @Enumerated(EnumType.STRING)
    private Role viewerRole;

    private LocalDateTime viewedAt = LocalDateTime.now();
}
