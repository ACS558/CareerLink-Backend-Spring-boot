package com.careerlink.careerlink_backend.entity;

import com.careerlink.careerlink_backend.entity.embeddable.PersonalInfo;
import com.careerlink.careerlink_backend.entity.enums.RoleLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "admins")
public class Admin extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    private RoleLevel roleLevel = RoleLevel.ADMIN;

    @Embedded
    private PersonalInfo personalInfo;

    private String department;
}
