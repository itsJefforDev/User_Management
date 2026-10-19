package com.userManagement.businessLayer.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountDTOs {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateAccountRequest {
        private String name;
        private String type;
        private BigDecimal initialBalance;
        private Long userId; // ID del usuario al que pertenece la cuenta
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateAccountRequest {
        private String name;
        private String type;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AccountResponse {
        private Long id;
        private String name;
        private String type;
        private BigDecimal balance;
        private Boolean active;
        private Long userId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}