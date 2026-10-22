package com.careerlink.careerlink_backend.entity.embeddable;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Embeddable
public class AtsScore {


    private Double score;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "strengths", columnDefinition = "text[]")
    private List<String> strengths = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "weaknesses", columnDefinition = "text[]")
    private List<String> weaknesses = new ArrayList<>();

    private String recommendation;

    @Column(columnDefinition = "TEXT", length = 5000)
    private String overallSummary;

    private LocalDateTime calculatedAt;
}
