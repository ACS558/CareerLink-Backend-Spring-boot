package com.careerlink.careerlink_backend.entity.embeddable;


import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class AcademicInfo {
    private String department;
    private String branch;
    private Integer semester;
    private Double cgpa;
    private Double percentage;
    private Integer backlogs = 0;
    private Integer graduationYear;
}
