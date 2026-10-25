package com.userManagement.userManagement.businessLayer.impl;

import com.userManagement.businessLayer.dto.CategoryDTOs.*;
import com.userManagement.businessLayer.service.impl.CategoryServiceImpl;
import com.userManagement.persistenceLayer.entity.Category;
import com.userManagement.persistenceLayer.mapper.CategoryMapper;
import com.userManagement.persistenceLayer.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests para CategoryServiceImpl
 *
 * OBJETIVO: Probar la lógica de negocio del servicio de categorías de forma aislada
 * - Cobertura del 100% de los métodos y caminos alternativos (creación, consultas por ID/tipo, actualización, borrado lógico)
 * - Sin base de datos ni Spring Context
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService - Unit Tests")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category validCategory;
    private CategoryResponse validCategoryResponse;
    private CreateCategoryRequest createCategoryRequest;
    private UpdateCategoryRequest updateCategoryRequest;

    @BeforeEach
    void setUp() {
        validCategory = Category.builder()
                .id(1L)
                .name("Alimentación")
                .description("Gastos en supermercado y comida")
                .type("EXPENSE")
                .active(true)
                .build();

        validCategoryResponse = CategoryResponse.builder()
                .id(1L)
                .name("Alimentación")
                .description("Gastos en supermercado y comida")
                .type("EXPENSE")
                .active(true)
                .build();

        createCategoryRequest = new CreateCategoryRequest();
        createCategoryRequest.setName("Alimentación");
        createCategoryRequest.setDescription("Gastos en supermercado y comida");
        createCategoryRequest.setType("EXPENSE");

        updateCategoryRequest = new UpdateCategoryRequest();
        updateCategoryRequest.setName("Alimentación Actualizada");
        updateCategoryRequest.setDescription("Nueva descripción");
        updateCategoryRequest.setType("EXPENSE");
        updateCategoryRequest.setActive(true);
    }

    // ==================== CREATE CATEGORY TESTS ====================

    @Test
    @DisplayName("CREATE - Datos válidos debe guardar y retornar CategoryResponse")
    void createCategory_ValidData_ShouldReturnCategoryResponse() {
        when(categoryRepository.save(any(Category.class))).thenReturn(validCategory);
        when(categoryMapper.toResponse(validCategory)).thenReturn(validCategoryResponse);

        CategoryResponse response = categoryService.createCategory(createCategoryRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Alimentación");
        assertThat(response.getType()).isEqualTo("EXPENSE");

        verify(categoryRepository).save(any(Category.class));
        verify(categoryMapper).toResponse(validCategory);
    }

    // ==================== GET CATEGORY BY ID TESTS ====================

    @Test
    @DisplayName("GET BY ID - ID existente debe retornar CategoryResponse")
    void getCategoryById_ExistingId_ShouldReturnCategoryResponse() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(validCategory));
        when(categoryMapper.toResponse(validCategory)).thenReturn(validCategoryResponse);

        CategoryResponse response = categoryService.getCategoryById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Alimentación");

        verify(categoryRepository).findById(1L);
        verify(categoryMapper).toResponse(validCategory);
    }

    @Test
    @DisplayName("GET BY ID - ID inexistente debe lanzar RuntimeException")
    void getCategoryById_NonExistingId_ShouldThrowRuntimeException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found with id: 99");

        verify(categoryRepository).findById(99L);
        verify(categoryMapper, never()).toResponse(any());
    }

    // ==================== GET ALL CATEGORIES TESTS ====================

    @Test
    @DisplayName("GET ALL - Debe retornar lista de CategoryResponse")
    void getAllCategories_ShouldReturnCategoryResponseList() {
        when(categoryRepository.findAll()).thenReturn(Collections.singletonList(validCategory));
        when(categoryMapper.toResponse(validCategory)).thenReturn(validCategoryResponse);

        List<CategoryResponse> responses = categoryService.getAllCategories();

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("Alimentación");

        verify(categoryRepository).findAll();
        verify(categoryMapper).toResponse(validCategory);
    }

    // ==================== GET CATEGORIES BY TYPE TESTS ====================

    @Test
    @DisplayName("GET BY TYPE - Tipo existente debe retornar lista de CategoryResponse")
    void getCategoriesByType_ExistingType_ShouldReturnCategoryResponseList() {
        when(categoryRepository.findByType("EXPENSE")).thenReturn(Arrays.asList(validCategory));
        when(categoryMapper.toResponse(validCategory)).thenReturn(validCategoryResponse);

        List<CategoryResponse> responses = categoryService.getCategoriesByType("EXPENSE");

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getType()).isEqualTo("EXPENSE");

        verify(categoryRepository).findByType("EXPENSE");
        verify(categoryMapper).toResponse(validCategory);
    }

    // ==================== UPDATE CATEGORY TESTS ====================

    @Test
    @DisplayName("UPDATE - Datos válidos (con active no nulo) debe actualizar y retornar CategoryResponse")
    void updateCategory_ValidData_ShouldReturnUpdatedCategoryResponse() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(validCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(validCategory);
        when(categoryMapper.toResponse(validCategory)).thenReturn(validCategoryResponse);

        CategoryResponse response = categoryService.updateCategory(1L, updateCategoryRequest);

        assertThat(response).isNotNull();
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
        verify(categoryMapper).toResponse(validCategory);
    }

    @Test
    @DisplayName("UPDATE - Solicitud con active nulo no debe modificar el estado active previo")
    void updateCategory_ActiveNull_ShouldNotModifyActiveField() {
        updateCategoryRequest.setActive(null);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(validCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(validCategory);
        when(categoryMapper.toResponse(validCategory)).thenReturn(validCategoryResponse);

        CategoryResponse response = categoryService.updateCategory(1L, updateCategoryRequest);

        assertThat(response).isNotNull();
        assertThat(validCategory.getActive()).isTrue(); // Se mantiene el valor original

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("UPDATE - ID inexistente debe lanzar RuntimeException")
    void updateCategory_NonExistingId_ShouldThrowRuntimeException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(99L, updateCategoryRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found with id: 99");

        verify(categoryRepository).findById(99L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    // ==================== DELETE CATEGORY TESTS ====================

    @Test
    @DisplayName("DELETE - ID existente debe realizar desactivación lógica (active = false) y guardar")
    void deleteCategory_ExistingId_ShouldSetCategoryInactive() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(validCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(validCategory);

        assertThatCode(() -> categoryService.deleteCategory(1L)).doesNotThrowAnyException();

        assertThat(validCategory.getActive()).isFalse();
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(validCategory);
    }

    @Test
    @DisplayName("DELETE - ID inexistente debe lanzar RuntimeException")
    void deleteCategory_NonExistingId_ShouldThrowRuntimeException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found with id: 99");

        verify(categoryRepository).findById(99L);
        verify(categoryRepository, never()).save(any(Category.class));
    }
}