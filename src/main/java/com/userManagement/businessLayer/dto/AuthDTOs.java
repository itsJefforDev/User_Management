package com.userManagement.businessLayer.dto;

import com.userManagement.persistenceLayer.entity.Role;
import com.userManagement.persistenceLayer.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

public class AuthDTOs {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginResponse {
        private String token; // O el mensaje/datos que estés manejando actualmente
        private String message;
        private Long userId;
        private String email;
        private String name;
        private UserStatus status;
        private Set<Role> role;
    }
}