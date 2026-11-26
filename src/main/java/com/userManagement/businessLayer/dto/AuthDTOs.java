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
        private String token; // null cuando mustChangePassword = true (no se abre sesión)
        private String message;
        private Long userId;
        private String email;
        private String name;
        private UserStatus status;
        private Set<Role> role;
        /** true si ingresó con una contraseña temporal vigente y debe cambiarla. */
        private Boolean mustChangePassword;
    }

    /** Verificación del código de 4 dígitos enviado al correo (auto-registro). */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyCodeRequest {
        private String email;
        private String code;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResendCodeRequest {
        private String email;
    }

    /** Recordatorio de contraseña: se envía una contraseña temporal al correo. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForgotPasswordRequest {
        private String email;
    }

    /** Cambio de contraseña (obligatorio tras usar una contraseña temporal). */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePasswordRequest {
        private String email;
        private String currentPassword;
        private String newPassword;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageResponse {
        private String message;
    }
}