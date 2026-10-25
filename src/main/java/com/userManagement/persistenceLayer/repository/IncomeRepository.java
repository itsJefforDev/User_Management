package com.userManagement.persistenceLayer.repository;

import com.userManagement.persistenceLayer.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long>, JpaSpecificationExecutor<Income> {
    List<Income> findByAccountId(Long accountId);
    List<Income> findByCategoryId(Long categoryId);
    List<Income> findByDateBetween(LocalDate startDate, LocalDate endDate);
}