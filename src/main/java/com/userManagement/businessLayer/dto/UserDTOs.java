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
        private String password;               // Admin: contraseña temporal de 8 dígitos
        private Set<String> roles;
        private Integer temporaryPasswordHours; // Admin: vigencia de la contraseña temporal (1-720, def. 24)
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateUserRequest {
        private String name;
        private UserStatus status;
        private Set<String> roles;
        private String password;               // Opcional: nueva contraseña temporal (8 dígitos)
        private Integer temporaryPasswordHours; // Vigencia de la nueva contraseña temporal
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
        private Boolean mustChangePassword;
        private LocalDateTime temporaryPasswordExpiresAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    /** Contraseña temporal aleatoria generada para el administrador. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TemporaryPasswordResponse {
        private String password;
    }
}