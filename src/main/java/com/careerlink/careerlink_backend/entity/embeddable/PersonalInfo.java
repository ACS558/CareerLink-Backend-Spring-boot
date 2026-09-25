package com.careerlink.careerlink_backend.entity.embeddable;


import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Embeddable
public class PersonalInfo {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String gender;
    private String profilePictureUrl;
    private String profilePicturePublicId;
}
