package com.userManagement.businessLayer.service;

import com.userManagement.businessLayer.dto.CategoryDTOs.*;
import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CreateCategoryRequest request);
    CategoryResponse getCategoryById(Long id);
    List<CategoryResponse> getAllCategories();
    List<CategoryResponse> getCategoriesByType(String type);
    CategoryResponse updateCategory(Long id, UpdateCategoryRequest request);
    void deleteCategory(Long id); // O desactivación lógica según prefieras
}