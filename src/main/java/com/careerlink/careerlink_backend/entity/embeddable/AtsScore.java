package com.careerlink.careerlink_backend.entity.embeddable;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Embeddable
public class AtsScore {

    private Double score;

    @ElementCollection
    @CollectionTable(name = "application_strengths", joinColumns = @JoinColumn(name = "application_id"))
    @Column(name = "strength")
    private List<String> strengths = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "application_weaknesses", joinColumns = @JoinColumn(name = "application_id"))
    @Column(name = "weakness")
    private List<String> weaknesses = new ArrayList<>();

    private String recommendation;

    @Column(columnDefinition = "TEXT", length = 5000)
    private String overallSummary;

    private LocalDateTime calculatedAt;
}
