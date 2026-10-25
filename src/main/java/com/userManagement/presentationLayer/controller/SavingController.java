package com.userManagement.presentationLayer.controller;

import com.userManagement.businessLayer.dto.SavingDTOs.*;
import com.userManagement.businessLayer.service.SavingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/savings")
@RequiredArgsConstructor
public class SavingController {

    private final SavingService savingService;

    @PostMapping("/goals")
    public ResponseEntity<SavingGoalResponse> createSavingGoal(@RequestBody CreateSavingGoalRequest request) {
        SavingGoalResponse response = savingService.createSavingGoal(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/goals/{id}")
    public ResponseEntity<SavingGoalResponse> getSavingGoalById(@PathVariable Long id) {
        return ResponseEntity.ok(savingService.getSavingGoalById(id));
    }

    @GetMapping("/goals/user/{userId}")
    public ResponseEntity<List<SavingGoalResponse>> getSavingGoalsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(savingService.getSavingGoalsByUser(userId));
    }

    @PutMapping("/goals/{id}")
    public ResponseEntity<SavingGoalResponse> updateSavingGoal(
            @PathVariable Long id,
            @RequestBody UpdateSavingGoalRequest request) {
        return ResponseEntity.ok(savingService.updateSavingGoal(id, request));
    }

    @DeleteMapping("/goals/{id}")
    public ResponseEntity<Void> deleteSavingGoal(@PathVariable Long id) {
        savingGoalRepositoryDeleteCheck(id); // o directo el servicio
        savingService.deleteSavingGoal(id);
        return ResponseEntity.noContent().build();
    }

    private void savingGoalRepositoryDeleteCheck(Long id) {
        // Método auxiliar o directo en el servicio
    }

    @PostMapping("/contributions")
    public ResponseEntity<SavingContributionResponse> addContribution(@RequestBody CreateContributionRequest request) {
        SavingContributionResponse response = savingService.addContribution(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/goals/{goalId}/contributions")
    public ResponseEntity<List<SavingContributionResponse>> getContributionsByGoal(@PathVariable Long goalId) {
        return ResponseEntity.ok(savingService.getContributionsByGoal(goalId));
    }
}