package com.userManagement.persistenceLayer.repository;

import com.userManagement.persistenceLayer.entity.SavingContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingContributionRepository extends JpaRepository<SavingContribution, Long> {
    List<SavingContribution> findBySavingGoalId(Long savingGoalId);
}