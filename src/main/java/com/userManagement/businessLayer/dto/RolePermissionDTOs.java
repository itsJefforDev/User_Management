package com.userManagement.businessLayer.dto;

import lombok.*;
import java.util.Set;

public class RolePermissionDTOs {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRoleRequest {
        private String name;
        private Set<String> permissions; // Nombres de los permisos a asociar
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoleResponse {
        private Long id;
        private String name;
        private Set<String> permissions;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePermissionRequest {
        private String name;
        private String description;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PermissionResponse {
        private Long id;
        private String name;
        private String description;
    }
}