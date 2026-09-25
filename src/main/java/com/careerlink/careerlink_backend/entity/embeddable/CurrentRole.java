package com.careerlink.careerlink_backend.entity.embeddable;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@Embeddable
public class CurrentRole {
    private String company;
    private String designation;
    private String location;
    private Integer experience = 0;
    private LocalDate startDate;
}
