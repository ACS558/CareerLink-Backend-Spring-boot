package com.careerlink.careerlink_backend.entity.embeddable;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class ContactPerson {
    private String name;
    private String designation;
    private String phoneNumber;
    private String email;
}
