package com.careerlink.careerlink_backend.entity.embeddable;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Embeddable
public class CloudinaryFile {
    private String url;
    private String publicId;
    private LocalDateTime uploadedAt;
}
