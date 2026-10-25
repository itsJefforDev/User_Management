package com.userManagement.persistenceLayer.repository;

import com.userManagement.persistenceLayer.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {
    List<Expense> findByAccountId(Long accountId);
    List<Expense> findByCategoryId(Long categoryId);
    List<Expense> findByDateBetween(LocalDate startDate, LocalDate endDate);
}