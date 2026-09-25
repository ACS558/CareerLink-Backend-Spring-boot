package com.careerlink.careerlink_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "student_certifications")
public class Certification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    private String name;
    private String issuedBy;
    private LocalDate issueDate;
    private String credentialUrl;
}
