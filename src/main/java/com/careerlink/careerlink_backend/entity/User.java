package com.careerlink.careerlink_backend.entity;

import com.careerlink.careerlink_backend.entity.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean isVerified = false;

    @Column(nullable = false)
    private boolean isActive = true;

    private LocalDateTime lastLogin;
    private String resetPasswordToken;
    private LocalDateTime resetPasswordExpire;
}
