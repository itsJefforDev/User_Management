package com.userManagement.businessLayer.dto;

import com.userManagement.persistenceLayer.entity.UserStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

public class UserDTOs {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateUserRequest {
        private String name;
        private String email;
        private String password;
        private Set<String> roles;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateUserRequest {
        private String name;
        private UserStatus status;
        private Set<String> roles;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserResponse {
        private Long id;
        private String name;
        private String email;
        private UserStatus status;
        private Integer failedLoginAttempts;
        private Set<String> roles;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}