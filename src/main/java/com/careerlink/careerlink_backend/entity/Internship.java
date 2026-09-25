package com.careerlink.careerlink_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "student_internships")
public class Internship extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    private String companyName;
    private String role;
    private String duration;
    private LocalDate startDate;
    private LocalDate endDate;
    @Column(columnDefinition = "TEXT")
    private String description;
}
