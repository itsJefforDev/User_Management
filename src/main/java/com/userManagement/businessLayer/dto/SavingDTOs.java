package com.userManagement.businessLayer.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SavingDTOs {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateSavingGoalRequest {
        private String name;
        private String description;
        private BigDecimal targetAmount;
        private LocalDate targetDate;
        private Long userId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateSavingGoalRequest {
        private String name;
        private String description;
        private BigDecimal targetAmount;
        private LocalDate targetDate;
        private String status;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SavingGoalResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal targetAmount;
        private BigDecimal currentAmount;
        private LocalDate targetDate;
        private String status;
        private Long userId;
        private Double progressPercentage;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateContributionRequest {
        private BigDecimal amount;
        private LocalDate date;
        private String notes;
        private Long savingGoalId;
        private Long accountId; // Opcional si se descuenta de una cuenta de banco
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SavingContributionResponse {
        private Long id;
        private BigDecimal amount;
        private LocalDate date;
        private String notes;
        private Long savingGoalId;
        private String accountName;
        private LocalDateTime createdAt;
    }
}