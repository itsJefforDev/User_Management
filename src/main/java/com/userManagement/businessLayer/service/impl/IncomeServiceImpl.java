package com.userManagement.businessLayer.service.impl;

import com.userManagement.businessLayer.dto.IncomeDTOs.*;
import com.userManagement.businessLayer.service.IncomeService;
import com.userManagement.persistenceLayer.entity.Account;
import com.userManagement.persistenceLayer.entity.Category;
import com.userManagement.persistenceLayer.entity.Income;
import com.userManagement.persistenceLayer.mapper.IncomeMapper;
import com.userManagement.persistenceLayer.repository.AccountRepository;
import com.userManagement.persistenceLayer.repository.CategoryRepository;
import com.userManagement.persistenceLayer.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final IncomeMapper incomeMapper;

    @Override
    @Transactional
    public IncomeResponse createIncome(CreateIncomeRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Cuenta bancaria no encontrada"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Income income = Income.builder()
                .amount(request.getAmount())
                .description(request.getDescription())
                .date(request.getDate())
                .category(category)
                .account(account)
                .incomeType(request.getIncomeType())
                .observations(request.getObservations())
                .build();

        // Opcional: Actualizar el balance de la cuenta sumando el ingreso
        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);

        Income savedIncome = incomeRepository.save(income);
        return incomeMapper.toResponse(savedIncome);
    }

    @Override
    @Transactional(readOnly = true)
    public IncomeResponse getIncomeById(Long id) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingreso no encontrado"));
        return incomeMapper.toResponse(income);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncomeResponse> getAllIncomes() {
        return incomeRepository.findAll().stream()
                .map(incomeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncomeResponse> getIncomesByAccount(Long accountId) {
        return incomeRepository.findByAccountId(accountId).stream()
                .map(incomeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncomeResponse> getIncomesByCategory(Long categoryId) {
        return incomeRepository.findByCategoryId(categoryId).stream()
                .map(incomeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncomeResponse> getIncomesByDateRange(LocalDate startDate, LocalDate endDate) {
        return incomeRepository.findByDateBetween(startDate, endDate).stream()
                .map(incomeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public IncomeResponse updateIncome(Long id, UpdateIncomeRequest request) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingreso no encontrado"));

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Cuenta bancaria no encontrada"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // Revertir el monto anterior en la cuenta vieja y sumar el nuevo (si cambió la cuenta o el monto)
        // (Lógica simplificada de actualización de montos)
        income.setAmount(request.getAmount());
        income.setDescription(request.getDescription());
        income.setDate(request.getDate());
        income.setCategory(category);
        income.setAccount(account);
        income.setIncomeType(request.getIncomeType());
        income.setObservations(request.getObservations());

        Income updatedIncome = incomeRepository.save(income);
        return incomeMapper.toResponse(updatedIncome);
    }

    @Override
    @Transactional
    public void deleteIncome(Long id) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingreso no encontrado"));
        incomeRepository.delete(income);
    }
}