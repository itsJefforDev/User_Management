package com.userManagement.businessLayer.service;

import com.userManagement.businessLayer.dto.ExpenseDTOs.*;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {
    ExpenseResponse createExpense(CreateExpenseRequest request);
    ExpenseResponse getExpenseById(Long id);
    List<ExpenseResponse> getAllExpenses();
    List<ExpenseResponse> getExpensesByAccount(Long accountId);
    List<ExpenseResponse> getExpensesByCategory(Long categoryId);
    List<ExpenseResponse> getExpensesByDateRange(LocalDate startDate, LocalDate endDate);
    ExpenseResponse updateExpense(Long id, UpdateExpenseRequest request);
    void deleteExpense(Long id);
}