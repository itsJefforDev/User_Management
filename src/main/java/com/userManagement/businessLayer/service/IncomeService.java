package com.userManagement.businessLayer.service;

import com.userManagement.businessLayer.dto.IncomeDTOs.*;
import java.time.LocalDate;
import java.util.List;

public interface IncomeService {
    IncomeResponse createIncome(CreateIncomeRequest request);
    IncomeResponse getIncomeById(Long id);
    List<IncomeResponse> getAllIncomes();
    List<IncomeResponse> getIncomesByAccount(Long accountId);
    List<IncomeResponse> getIncomesByCategory(Long categoryId);
    List<IncomeResponse> getIncomesByDateRange(LocalDate startDate, LocalDate endDate);
    IncomeResponse updateIncome(Long id, UpdateIncomeRequest request);
    void deleteIncome(Long id);
}