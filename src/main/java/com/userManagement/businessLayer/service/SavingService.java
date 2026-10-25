package com.userManagement.businessLayer.service;

import com.userManagement.businessLayer.dto.SavingDTOs.*;
import java.util.List;

public interface SavingService {
    SavingGoalResponse createSavingGoal(CreateSavingGoalRequest request);
    SavingGoalResponse getSavingGoalById(Long id);
    List<SavingGoalResponse> getSavingGoalsByUser(Long userId);
    SavingGoalResponse updateSavingGoal(Long id, UpdateSavingGoalRequest request);
    void deleteSavingGoal(Long id);

    // Aportes
    SavingContributionResponse addContribution(CreateContributionRequest request);
    List<SavingContributionResponse> getContributionsByGoal(Long savingGoalId);
}