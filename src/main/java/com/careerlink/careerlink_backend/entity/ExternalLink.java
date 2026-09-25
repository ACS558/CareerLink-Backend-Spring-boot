package com.careerlink.careerlink_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "student_external_links")
public class ExternalLink extends BaseEntity {

    // nullable — a link belongs to EITHER a student OR an alumni, never both
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumni_id")
    private Alumni alumni;

    private String name;
    private String url;
}