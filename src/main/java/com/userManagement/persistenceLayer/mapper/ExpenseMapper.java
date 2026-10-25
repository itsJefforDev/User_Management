package com.userManagement.persistenceLayer.mapper;

import com.userManagement.businessLayer.dto.ExpenseDTOs.*;
import com.userManagement.persistenceLayer.entity.Expense;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    public ExpenseResponse toResponse(Expense expense) {
        if (expense == null) return null;

        return ExpenseResponse.builder()
                .id(expense.getId())
                .amount(expense.getAmount())
                .description(expense.getDescription())
                .date(expense.getDate())
                .categoryName(expense.getCategory() != null ? expense.getCategory().getName() : null)
                .accountName(expense.getAccount() != null ? expense.getAccount().getName() : null)
                .expenseType(expense.getExpenseType())
                .observations(expense.getObservations())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}