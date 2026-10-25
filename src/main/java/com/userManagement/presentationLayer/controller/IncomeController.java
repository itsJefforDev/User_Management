package com.userManagement.presentationLayer.controller;

import com.userManagement.businessLayer.dto.IncomeDTOs.*;
import com.userManagement.businessLayer.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/incomes")
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<IncomeResponse> createIncome(@RequestBody CreateIncomeRequest request) {
        IncomeResponse response = incomeService.createIncome(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncomeResponse> getIncomeById(@PathVariable Long id) {
        return ResponseEntity.ok(incomeService.getIncomeById(id));
    }

    @GetMapping
    public ResponseEntity<List<IncomeResponse>> getAllIncomes() {
        return ResponseEntity.ok(incomeService.getAllIncomes());
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<IncomeResponse>> getIncomesByAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(incomeService.getIncomesByAccount(accountId));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<IncomeResponse>> getIncomesByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(incomeService.getIncomesByCategory(categoryId));
    }

    @GetMapping("/filter/date")
    public ResponseEntity<List<IncomeResponse>> getIncomesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(incomeService.getIncomesByDateRange(startDate, endDate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncomeResponse> updateIncome(
            @PathVariable Long id,
            @RequestBody UpdateIncomeRequest request) {
        return ResponseEntity.ok(incomeService.updateIncome(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(@PathVariable Long id) {
        incomeService.deleteIncome(id);
        return ResponseEntity.noContent().build();
    }
}