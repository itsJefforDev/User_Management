package com.userManagement.businessLayer.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExpenseDTOs {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateExpenseRequest {
        private BigDecimal amount;
        private String description;
        private LocalDate date;
        private Long categoryId;
        private Long accountId;
        private String expenseType;
        private String observations;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateExpenseRequest {
        private BigDecimal amount;
        private String description;
        private LocalDate date;
        private Long categoryId;
        private Long accountId;
        private String expenseType;
        private String observations;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExpenseResponse {
        private Long id;
        private BigDecimal amount;
        private String description;
        private LocalDate date;
        private String categoryName;
        private String accountName;
        private String expenseType;
        private String observations;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}