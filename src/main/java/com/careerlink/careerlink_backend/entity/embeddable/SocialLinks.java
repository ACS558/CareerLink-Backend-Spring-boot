package com.careerlink.careerlink_backend.entity.embeddable;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class SocialLinks {
    private String linkedin;
    private String github;
    private String portfolio;
    private String twitter;

}
