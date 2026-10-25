package com.userManagement.businessLayer.dto;

import lombok.*;
import java.time.LocalDateTime;

public class CategoryDTOs {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateCategoryRequest {
        private String name;
        private String description;
        private String type; // EXPENSE, INCOME, BOTH
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateCategoryRequest {
        private String name;
        private String description;
        private String type;
        private Boolean active;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryResponse {
        private Long id;
        private String name;
        private String description;
        private String type;
        private Boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}