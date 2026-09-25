package com.careerlink.careerlink_backend.config;

import lombok.Getter;

import java.security.Principal;

public class StompPrincipal implements Principal {

    private final String email;
    @Getter
    private final Long userId;

    public StompPrincipal(String email, Long userId) {
        this.email = email;
        this.userId = userId;
    }

    @Override
    public String getName() {
        return email;
    }

}
