package com.userManagement.userManagement.businessLayer.impl;

import com.userManagement.businessLayer.dto.ExpenseDTOs.*;
import com.userManagement.businessLayer.service.impl.ExpenseServiceImpl;
import com.userManagement.persistenceLayer.entity.Account;
import com.userManagement.persistenceLayer.entity.Category;
import com.userManagement.persistenceLayer.entity.Expense;
import com.userManagement.persistenceLayer.mapper.ExpenseMapper;
import com.userManagement.persistenceLayer.repository.AccountRepository;
import com.userManagement.persistenceLayer.repository.CategoryRepository;
import com.userManagement.persistenceLayer.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests para ExpenseServiceImpl
 *
 * OBJETIVO: Probar la lógica de negocio del servicio de gastos de forma aislada
 * - Cobertura del 100% de los métodos y caminos alternativos
 * - Sin base de datos ni Spring Context
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExpenseService - Unit Tests")
class ExpenseServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ExpenseMapper expenseMapper;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    private Account validAccount;
    private Category validCategory;
    private Expense validExpense;
    private CreateExpenseRequest createExpenseRequest;
    private UpdateExpenseRequest updateExpenseRequest;
    private ExpenseResponse expenseResponse;

    @BeforeEach
    void setUp() {
        validAccount = Account.builder()
                .id(1L)
                .name("Cuenta Corriente")
                .balance(new BigDecimal("1000.00"))
                .build();

        validCategory = Category.builder()
                .id(1L)
                .name("Alimentación")
                .type("EXPENSE")
                .active(true)
                .build();

        validExpense = Expense.builder()
                .id(1L)
                .amount(new BigDecimal("150.00"))
                .description("Supermercado")
                .date(LocalDate.of(2026, 6, 1))
                .account(validAccount)
                .category(validCategory)
                .expenseType("VARIABLE")
                .observations("Compra semanal")
                .build();

        createExpenseRequest = new CreateExpenseRequest(
                new BigDecimal("150.00"),
                "Supermercado",
                LocalDate.of(2026, 6, 1),
                1L,
                1L,
                "VARIABLE",
                "Compra semanal"
        );

        updateExpenseRequest = new UpdateExpenseRequest(
                new BigDecimal("200.00"),
                "Supermercado Actualizado",
                LocalDate.of(2026, 6, 2),
                1L,
                1L,
                "VARIABLE",
                "Observación actualizada"
        );

        expenseResponse = ExpenseResponse.builder()
                .id(1L)
                .amount(new BigDecimal("150.00"))
                .description("Supermercado")
                .date(LocalDate.of(2026, 6, 1))
                .categoryName("Alimentación")
                .accountName("Cuenta Corriente")
                .expenseType("VARIABLE")
                .observations("Compra semanal")
                .build();
    }

    // ==================== CREATE EXPENSE TESTS ====================

    @Test
    @DisplayName("CREATE - Datos válidos debe guardar y retornar ExpenseResponse")
    void createExpense_ValidData_ShouldReturnExpenseResponse() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(validAccount));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(validCategory));
        when(expenseRepository.save(any(Expense.class))).thenReturn(validExpense);
        when(expenseMapper.toResponse(validExpense)).thenReturn(expenseResponse);

        ExpenseResponse response = expenseService.createExpense(createExpenseRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getAmount()).isEqualTo(new BigDecimal("150.00"));
        assertThat(response.getCategoryName()).isEqualTo("Alimentación");
        assertThat(response.getAccountName()).isEqualTo("Cuenta Corriente");

        verify(accountRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(expenseRepository).save(any(Expense.class));
        verify(expenseMapper).toResponse(validExpense);
    }

    @Test
    @DisplayName("CREATE - Cuenta no encontrada debe lanzar RuntimeException")
    void createExpense_AccountNotFound_ShouldThrowRuntimeException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.createExpense(createExpenseRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Account not found with id: 1");

        verify(accountRepository).findById(1L);
        verify(categoryRepository, never()).findById(any());
        verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("CREATE - Categoría no encontrada debe lanzar RuntimeException")
    void createExpense_CategoryNotFound_ShouldThrowRuntimeException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(validAccount));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.createExpense(createExpenseRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found with id: 1");

        verify(accountRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(expenseRepository, never()).save(any());
    }

    // ==================== GET EXPENSE BY ID TESTS ====================

    @Test
    @DisplayName("GET BY ID - ID existente debe retornar ExpenseResponse")
    void getExpenseById_ExistingId_ShouldReturnExpenseResponse() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(validExpense));
        when(expenseMapper.toResponse(validExpense)).thenReturn(expenseResponse);

        ExpenseResponse response = expenseService.getExpenseById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        verify(expenseRepository).findById(1L);
        verify(expenseMapper).toResponse(validExpense);
    }

    @Test
    @DisplayName("GET BY ID - ID inexistente debe lanzar RuntimeException")
    void getExpenseById_NonExistingId_ShouldThrowRuntimeException() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.getExpenseById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Expense not found with id: 99");

        verify(expenseRepository).findById(99L);
        verify(expenseMapper, never()).toResponse(any());
    }

    // ==================== GET ALL EXPENSES TESTS ====================

    @Test
    @DisplayName("GET ALL - Debe retornar lista de ExpenseResponse")
    void getAllExpenses_ShouldReturnExpenseResponseList() {
        when(expenseRepository.findAll()).thenReturn(Collections.singletonList(validExpense));
        when(expenseMapper.toResponse(validExpense)).thenReturn(expenseResponse);

        List<ExpenseResponse> responses = expenseService.getAllExpenses();

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);

        verify(expenseRepository).findAll();
        verify(expenseMapper).toResponse(validExpense);
    }

    // ==================== GET EXPENSES BY ACCOUNT TESTS ====================

    @Test
    @DisplayName("GET BY ACCOUNT - AccountId existente debe retornar lista de ExpenseResponse")
    void getExpensesByAccount_ExistingAccountId_ShouldReturnList() {
        when(expenseRepository.findByAccountId(1L)).thenReturn(Collections.singletonList(validExpense));
        when(expenseMapper.toResponse(validExpense)).thenReturn(expenseResponse);

        List<ExpenseResponse> responses = expenseService.getExpensesByAccount(1L);

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        verify(expenseRepository).findByAccountId(1L);
    }

    // ==================== GET EXPENSES BY CATEGORY TESTS ====================

    @Test
    @DisplayName("GET BY CATEGORY - CategoryId existente debe retornar lista de ExpenseResponse")
    void getExpensesByCategory_ExistingCategoryId_ShouldReturnList() {
        when(expenseRepository.findByCategoryId(1L)).thenReturn(Collections.singletonList(validExpense));
        when(expenseMapper.toResponse(validExpense)).thenReturn(expenseResponse);

        List<ExpenseResponse> responses = expenseService.getExpensesByCategory(1L);

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        verify(expenseRepository).findByCategoryId(1L);
    }

    // ==================== GET EXPENSES BY DATE RANGE TESTS ====================

    @Test
    @DisplayName("GET BY DATE RANGE - Fechas válidas debe retornar lista de ExpenseResponse")
    void getExpensesByDateRange_ValidDates_ShouldReturnList() {
        LocalDate startDate = LocalDate.of(2026, 6, 1);
        LocalDate endDate = LocalDate.of(2026, 6, 30);

        when(expenseRepository.findByDateBetween(startDate, endDate)).thenReturn(Collections.singletonList(validExpense));
        when(expenseMapper.toResponse(validExpense)).thenReturn(expenseResponse);

        List<ExpenseResponse> responses = expenseService.getExpensesByDateRange(startDate, endDate);

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        verify(expenseRepository).findByDateBetween(startDate, endDate);
    }

    // ==================== UPDATE EXPENSE TESTS ====================

    @Test
    @DisplayName("UPDATE - Datos válidos debe actualizar y retornar ExpenseResponse")
    void updateExpense_ValidData_ShouldReturnUpdatedResponse() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(validExpense));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(validAccount));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(validCategory));
        when(expenseRepository.save(any(Expense.class))).thenReturn(validExpense);
        when(expenseMapper.toResponse(validExpense)).thenReturn(expenseResponse);

        ExpenseResponse response = expenseService.updateExpense(1L, updateExpenseRequest);

        assertThat(response).isNotNull();
        verify(expenseRepository).findById(1L);
        verify(accountRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(expenseRepository).save(any(Expense.class));
        verify(expenseMapper).toResponse(validExpense);
    }

    @Test
    @DisplayName("UPDATE - Gasto inexistente debe lanzar RuntimeException")
    void updateExpense_ExpenseNotFound_ShouldThrowRuntimeException() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.updateExpense(99L, updateExpenseRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Expense not found with id: 99");

        verify(expenseRepository).findById(99L);
        verify(accountRepository, never()).findById(any());
        verify(categoryRepository, never()).findById(any());
        verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("UPDATE - Cuenta inexistente en actualización debe lanzar RuntimeException")
    void updateExpense_AccountNotFound_ShouldThrowRuntimeException() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(validExpense));
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.updateExpense(1L, updateExpenseRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Account not found with id: 1");

        verify(expenseRepository).findById(1L);
        verify(accountRepository).findById(1L);
        verify(categoryRepository, never()).findById(any());
        verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("UPDATE - Categoría inexistente en actualización debe lanzar RuntimeException")
    void updateExpense_CategoryNotFound_ShouldThrowRuntimeException() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(validExpense));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(validAccount));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.updateExpense(1L, updateExpenseRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found with id: 1");

        verify(expenseRepository).findById(1L);
        verify(accountRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(expenseRepository, never()).save(any());
    }

    // ==================== DELETE EXPENSE TESTS ====================

    @Test
    @DisplayName("DELETE - ID existente debe eliminar el gasto correctamente")
    void deleteExpense_ExistingId_ShouldDeleteSuccessfully() {
        when(expenseRepository.existsById(1L)).thenReturn(true);
        doNothing().when(expenseRepository).deleteById(1L);

        assertThatCode(() -> expenseService.deleteExpense(1L)).doesNotThrowAnyException();

        verify(expenseRepository).existsById(1L);
        verify(expenseRepository).deleteById(1L);
    }

    @Test
    @DisplayName("DELETE - ID inexistente debe lanzar RuntimeException")
    void deleteExpense_NonExistingId_ShouldThrowRuntimeException() {
        when(expenseRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> expenseService.deleteExpense(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Expense not found with id: 99");

        verify(expenseRepository).existsById(99L);
        verify(expenseRepository, never()).deleteById(any());
    }
}