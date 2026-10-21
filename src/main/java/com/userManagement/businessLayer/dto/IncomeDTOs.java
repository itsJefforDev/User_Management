package com.userManagement.businessLayer.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class IncomeDTOs {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateIncomeRequest {
        private BigDecimal amount;
        private String description;
        private LocalDate date;
        private Long categoryId;
        private Long accountId;
        private String incomeType;
        private String observations;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateIncomeRequest {
        private BigDecimal amount;
        private String description;
        private LocalDate date;
        private Long categoryId;
        private Long accountId;
        private String incomeType;
        private String observations;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IncomeResponse {
        private Long id;
        private BigDecimal amount;
        private String description;
        private LocalDate date;
        private String categoryName;
        private String accountName;
        private String incomeType;
        private String observations;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}