package com.userManagement.businessLayer.service.impl;

import com.userManagement.businessLayer.dto.ExpenseDTOs.*;
import com.userManagement.businessLayer.service.ExpenseService;
import com.userManagement.persistenceLayer.entity.Account;
import com.userManagement.persistenceLayer.entity.Category;
import com.userManagement.persistenceLayer.entity.Expense;
import com.userManagement.persistenceLayer.mapper.ExpenseMapper;
import com.userManagement.persistenceLayer.repository.AccountRepository;
import com.userManagement.persistenceLayer.repository.CategoryRepository;
import com.userManagement.persistenceLayer.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseMapper expenseMapper;

    @Override
    @Transactional
    public ExpenseResponse createExpense(CreateExpenseRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + request.getAccountId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));

        Expense expense = Expense.builder()
                .amount(request.getAmount())
                .description(request.getDescription())
                .date(request.getDate())
                .account(account)
                .category(category)
                .expenseType(request.getExpenseType())
                .observations(request.getObservations())
                .build();

        Expense savedExpense = expenseRepository.save(expense);
        return expenseMapper.toResponse(savedExpense);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));
        return expenseMapper.toResponse(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getAllExpenses() {
        return expenseRepository.findAll().stream()
                .map(expenseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByAccount(Long accountId) {
        return expenseRepository.findByAccountId(accountId).stream()
                .map(expenseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByCategory(Long categoryId) {
        return expenseRepository.findByCategoryId(categoryId).stream()
                .map(expenseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByDateBetween(startDate, endDate).stream()
                .map(expenseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ExpenseResponse updateExpense(Long id, UpdateExpenseRequest request) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + request.getAccountId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));

        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setDate(request.getDate());
        expense.setAccount(account);
        expense.setCategory(category);
        expense.setExpenseType(request.getExpenseType());
        expense.setObservations(request.getObservations());

        Expense updatedExpense = expenseRepository.save(expense);
        return expenseMapper.toResponse(updatedExpense);
    }

    @Override
    @Transactional
    public void deleteExpense(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new RuntimeException("Expense not found with id: " + id);
        }
        expenseRepository.deleteById(id);
    }
}