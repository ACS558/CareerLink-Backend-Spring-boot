package com.careerlink.careerlink_backend.entity;

import com.careerlink.careerlink_backend.entity.enums.ExtensionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "student_extension_requests")
public class ExtensionRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Lob
    private String reason;

    @Enumerated(EnumType.STRING)
    private ExtensionStatus status = ExtensionStatus.PENDING;

    private LocalDateTime requestedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private Admin reviewedBy;

    private LocalDateTime reviewedAt;
}
