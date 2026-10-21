package com.userManagement.businessLayer.service.impl;

import com.userManagement.businessLayer.dto.SavingDTOs.*;
import com.userManagement.businessLayer.service.SavingService;
import com.userManagement.persistenceLayer.entity.Account;
import com.userManagement.persistenceLayer.entity.SavingContribution;
import com.userManagement.persistenceLayer.entity.SavingGoal;
import com.userManagement.persistenceLayer.entity.User;
import com.userManagement.persistenceLayer.mapper.SavingMapper;
import com.userManagement.persistenceLayer.repository.AccountRepository;
import com.userManagement.persistenceLayer.repository.SavingContributionRepository;
import com.userManagement.persistenceLayer.repository.SavingGoalRepository;
import com.userManagement.persistenceLayer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavingServiceImpl implements SavingService {

    private final SavingGoalRepository savingGoalRepository;
    private final SavingContributionRepository contributionRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final SavingMapper savingMapper;

    @Override
    @Transactional
    public SavingGoalResponse createSavingGoal(CreateSavingGoalRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + request.getUserId()));

        SavingGoal goal = SavingGoal.builder()
                .name(request.getName())
                .description(request.getDescription())
                .targetAmount(request.getTargetAmount())
                .currentAmount(BigDecimal.ZERO)
                .targetDate(request.getTargetDate())
                .status("IN_PROGRESS")
                .user(user)
                .build();

        SavingGoal savedGoal = savingGoalRepository.save(goal);
        return savingMapper.toGoalResponse(savedGoal);
    }

    @Override
    public SavingGoalResponse getSavingGoalById(Long id) {
        SavingGoal goal = savingGoalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meta de ahorro no encontrada con ID: " + id));
        return savingMapper.toGoalResponse(goal);
    }

    @Override
    public List<SavingGoalResponse> getSavingGoalsByUser(Long userId) {
        return savingGoalRepository.findByUserId(userId).stream()
                .map(savingMapper::toGoalResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SavingGoalResponse updateSavingGoal(Long id, UpdateSavingGoalRequest request) {
        SavingGoal goal = savingGoalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meta de ahorro no encontrada con ID: " + id));

        goal.setName(request.getName());
        goal.setDescription(request.getDescription());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());
        if (request.getStatus() != null) {
            goal.setStatus(request.getStatus());
        }

        SavingGoal updatedGoal = savingGoalRepository.save(goal);
        return savingMapper.toGoalResponse(updatedGoal);
    }

    @Override
    @Transactional
    public void deleteSavingGoal(Long id) {
        if (!savingGoalRepository.existsById(id)) {
            throw new RuntimeException("Meta de ahorro no encontrada con ID: " + id);
        }
        savingGoalRepository.deleteById(id);
    }

    @Override
    @Transactional
    public SavingContributionResponse addContribution(CreateContributionRequest request) {
        SavingGoal goal = savingGoalRepository.findById(request.getSavingGoalId())
                .orElseThrow(() -> new RuntimeException("Meta de ahorro no encontrada"));

        Account account = null;
        if (request.getAccountId() != null) {
            account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Cuenta bancaria no encontrada"));

            // Opcional: Validar saldo o descontar de la cuenta si aplica la regla de negocio
        }

        SavingContribution contribution = SavingContribution.builder()
                .amount(request.getAmount())
                .date(request.getDate())
                .notes(request.getNotes())
                .savingGoal(goal)
                .account(account)
                .build();

        // Actualizamos el monto actual acumulado en la meta
        goal.setCurrentAmount(goal.getCurrentAmount().add(request.getAmount()));

        // Si la meta se completa o supera, podemos actualizar el estado automáticamente
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus("COMPLETED");
        }

        savingGoalRepository.save(goal);
        SavingContribution savedContribution = contributionRepository.save(contribution);

        return savingMapper.toContributionResponse(savedContribution);
    }

    @Override
    public List<SavingContributionResponse> getContributionsByGoal(Long savingGoalId) {
        return contributionRepository.findBySavingGoalId(savingGoalId).stream()
                .map(savingMapper::toContributionResponse)
                .collect(Collectors.toList());
    }
}