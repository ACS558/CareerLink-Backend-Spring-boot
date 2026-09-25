package com.careerlink.careerlink_backend.entity.embeddable;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class CompanyInfo {
    private String companyName;
    private String industry;
    private String location;
    private String companyLogoUrl;
    private String website;
    private String companySize;
    private String description;
}
