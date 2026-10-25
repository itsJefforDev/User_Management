package com.userManagement.persistenceLayer.mapper;

import com.userManagement.businessLayer.dto.SavingDTOs.*;
import com.userManagement.persistenceLayer.entity.SavingContribution;
import com.userManagement.persistenceLayer.entity.SavingGoal;
import org.springframework.stereotype.Component;

@Component
public class SavingMapper {

    public SavingGoalResponse toGoalResponse(SavingGoal goal) {
        if (goal == null) return null;

        // Cálculo rápido del porcentaje de progreso
        double progressPercentage = 0.0;
        if (goal.getTargetAmount() != null && goal.getTargetAmount().doubleValue() > 0) {
            progressPercentage = goal.getCurrentAmount().doubleValue() / goal.getTargetAmount().doubleValue() * 100.0;
        }

        return SavingGoalResponse.builder()
                .id(goal.getId())
                .name(goal.getName())
                .description(goal.getDescription())
                .targetAmount(goal.getTargetAmount())
                .currentAmount(goal.getCurrentAmount())
                .targetDate(goal.getTargetDate())
                .status(goal.getStatus())
                .userId(goal.getUser() != null ? goal.getUser().getId() : null)
                .progressPercentage(progressPercentage)
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }

    public SavingContributionResponse toContributionResponse(SavingContribution contribution) {
        if (contribution == null) return null;

        return SavingContributionResponse.builder()
                .id(contribution.getId())
                .amount(contribution.getAmount())
                .date(contribution.getDate())
                .notes(contribution.getNotes())
                .savingGoalId(contribution.getSavingGoal() != null ? contribution.getSavingGoal().getId() : null)
                .accountName(contribution.getAccount() != null ? contribution.getAccount().getName() : null)
                .createdAt(contribution.getCreatedAt())
                .build();
    }
}