package com.userManagement.persistenceLayer.mapper;

import com.userManagement.businessLayer.dto.IncomeDTOs.*;
import com.userManagement.persistenceLayer.entity.Income;
import org.springframework.stereotype.Component;

@Component
public class IncomeMapper {

    public IncomeResponse toResponse(Income income) {
        if (income == null) return null;

        return IncomeResponse.builder()
                .id(income.getId())
                .amount(income.getAmount())
                .description(income.getDescription())
                .date(income.getDate())
                .categoryName(income.getCategory() != null ? income.getCategory().getName() : null)
                .accountName(income.getAccount() != null ? income.getAccount().getName() : null)
                .incomeType(income.getIncomeType())
                .observations(income.getObservations())
                .createdAt(income.getCreatedAt())
                .updatedAt(income.getUpdatedAt())
                .build();
    }
}