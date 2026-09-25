package com.careerlink.careerlink_backend.entity;

import com.careerlink.careerlink_backend.entity.embeddable.CloudinaryFile;
import com.careerlink.careerlink_backend.entity.enums.PlacementOfferStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "student_placements")
public class StudentPlacement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    private String company;
    private String jobTitle;
    private Double packageOffered;
    private LocalDate offerDate;
    private LocalDate joiningDate;

    @Enumerated(EnumType.STRING)
    private PlacementOfferStatus status = PlacementOfferStatus.PENDING;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "url", column = @Column(name = "offer_letter_url")),
            @AttributeOverride(name = "publicId", column = @Column(name = "offer_letter_public_id"))
    })
    private CloudinaryFile offerLetter;

    private boolean isPrimary = false;
    private Long relatedJobId;
    private Long relatedApplicationId;
}
