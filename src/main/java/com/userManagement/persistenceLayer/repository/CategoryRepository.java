package com.userManagement.persistenceLayer.repository;

import com.userManagement.persistenceLayer.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByType(String type);
    List<Category> findByActiveTrue();
    List<Category> findByTypeAndActiveTrue(String type);
}