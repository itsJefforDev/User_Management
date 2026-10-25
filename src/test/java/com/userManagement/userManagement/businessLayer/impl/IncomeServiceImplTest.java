package com.userManagement.userManagement.businessLayer.impl;

import com.userManagement.businessLayer.dto.IncomeDTOs.*;
import com.userManagement.businessLayer.service.impl.IncomeServiceImpl;
import com.userManagement.persistenceLayer.entity.Account;
import com.userManagement.persistenceLayer.entity.Category;
import com.userManagement.persistenceLayer.entity.Income;
import com.userManagement.persistenceLayer.mapper.IncomeMapper;
import com.userManagement.persistenceLayer.repository.AccountRepository;
import com.userManagement.persistenceLayer.repository.CategoryRepository;
import com.userManagement.persistenceLayer.repository.IncomeRepository;
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
 * Unit Tests para IncomeServiceImpl
 *
 * OBJETIVO: Probar la lógica de negocio del servicio de ingresos de forma aislada
 * - Cobertura del 100% de los métodos y caminos alternativos (creación, consultas múltiples, actualización, borrado, validaciones)
 * - Sin base de datos ni Spring Context
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("IncomeService - Unit Tests")
class IncomeServiceImplTest {

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private IncomeMapper incomeMapper;

    @InjectMocks
    private IncomeServiceImpl incomeService;

    private Account validAccount;
    private Category validCategory;
    private Income validIncome;
    private CreateIncomeRequest createIncomeRequest;
    private UpdateIncomeRequest updateIncomeRequest;
    private IncomeResponse incomeResponse;

    @BeforeEach
    void setUp() {
        validAccount = Account.builder()
                .id(1L)
                .name("Cuenta Corriente")
                .balance(new BigDecimal("1000.00"))
                .build();

        validCategory = Category.builder()
                .id(1L)
                .name("Salario")
                .build();

        validIncome = Income.builder()
                .id(1L)
                .amount(new BigDecimal("500.00"))
                .description("Pago mensual")
                .date(LocalDate.of(2026, 10, 1))
                .category(validCategory)
                .account(validAccount)
                .incomeType("REGULAR")
                .observations("Ninguna")
                .build();

        createIncomeRequest = new CreateIncomeRequest();
        createIncomeRequest.setAmount(new BigDecimal("500.00"));
        createIncomeRequest.setDescription("Pago mensual");
        createIncomeRequest.setDate(LocalDate.of(2026, 10, 1));
        createIncomeRequest.setAccountId(1L);
        createIncomeRequest.setCategoryId(1L);
        createIncomeRequest.setIncomeType("REGULAR");
        createIncomeRequest.setObservations("Ninguna");

        updateIncomeRequest = new UpdateIncomeRequest();
        updateIncomeRequest.setAmount(new BigDecimal("600.00"));
        updateIncomeRequest.setDescription("Pago actualizado");
        updateIncomeRequest.setDate(LocalDate.of(2026, 10, 2));
        updateIncomeRequest.setAccountId(1L);
        updateIncomeRequest.setCategoryId(1L);
        updateIncomeRequest.setIncomeType("BONUS");
        updateIncomeRequest.setObservations("Actualizado");

        incomeResponse = IncomeResponse.builder()
                .id(1L)
                .amount(new BigDecimal("500.00"))
                .description("Pago mensual")
                .date(LocalDate.of(2026, 10, 1))
                .accountName("Cuenta Corriente")
                .categoryName("Salario")
                .incomeType("REGULAR")
                .build();
    }

    // ==================== CREATE INCOME TESTS ====================

    @Test
    @DisplayName("CREATE - Datos válidos debe actualizar balance de cuenta, guardar y retornar IncomeResponse")
    void createIncome_ValidData_ShouldReturnIncomeResponse() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(validAccount));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(validCategory));
        when(accountRepository.save(any(Account.class))).thenReturn(validAccount);
        when(incomeRepository.save(any(Income.class))).thenReturn(validIncome);
        when(incomeMapper.toResponse(validIncome)).thenReturn(incomeResponse);

        IncomeResponse response = incomeService.createIncome(createIncomeRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(validAccount.getBalance()).isEqualByComparingTo(new BigDecimal("1500.00")); // 1000.00 + 500.00

        verify(accountRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(accountRepository).save(validAccount);
        verify(incomeRepository).save(any(Income.class));
        verify(incomeMapper).toResponse(validIncome);
    }

    @Test
    @DisplayName("CREATE - Cuenta bancaria no encontrada debe lanzar RuntimeException")
    void createIncome_AccountNotFound_ShouldThrowRuntimeException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incomeService.createIncome(createIncomeRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cuenta bancaria no encontrada");

        verify(accountRepository).findById(1L);
        verify(categoryRepository, never()).findById(any());
        verify(incomeRepository, never()).save(any());
    }

    @Test
    @DisplayName("CREATE - Categoría no encontrada debe lanzar RuntimeException")
    void createIncome_CategoryNotFound_ShouldThrowRuntimeException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(validAccount));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incomeService.createIncome(createIncomeRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Categoría no encontrada");

        verify(accountRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(incomeRepository, never()).save(any());
    }

    // ==================== GET INCOME BY ID TESTS ====================

    @Test
    @DisplayName("GET BY ID - ID existente debe retornar IncomeResponse")
    void getIncomeById_ExistingId_ShouldReturnIncomeResponse() {
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(validIncome));
        when(incomeMapper.toResponse(validIncome)).thenReturn(incomeResponse);

        IncomeResponse response = incomeService.getIncomeById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);

        verify(incomeRepository).findById(1L);
        verify(incomeMapper).toResponse(validIncome);
    }

    @Test
    @DisplayName("GET BY ID - ID inexistente debe lanzar RuntimeException")
    void getIncomeById_NonExistingId_ShouldThrowRuntimeException() {
        when(incomeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incomeService.getIncomeById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ingreso no encontrado");

        verify(incomeRepository).findById(99L);
        verify(incomeMapper, never()).toResponse(any());
    }

    // ==================== GET ALL INCOMES TESTS ====================

    @Test
    @DisplayName("GET ALL - Debe retornar lista de IncomeResponse")
    void getAllIncomes_ShouldReturnIncomeResponseList() {
        when(incomeRepository.findAll()).thenReturn(Collections.singletonList(validIncome));
        when(incomeMapper.toResponse(validIncome)).thenReturn(incomeResponse);

        List<IncomeResponse> responses = incomeService.getAllIncomes();

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);

        verify(incomeRepository).findAll();
        verify(incomeMapper).toResponse(validIncome);
    }

    // ==================== GET INCOMES BY ACCOUNT TESTS ====================

    @Test
    @DisplayName("GET BY ACCOUNT - Debe retornar lista de IncomeResponse filtrada por cuenta")
    void getIncomesByAccount_ShouldReturnIncomeResponseList() {
        when(incomeRepository.findByAccountId(1L)).thenReturn(Collections.singletonList(validIncome));
        when(incomeMapper.toResponse(validIncome)).thenReturn(incomeResponse);

        List<IncomeResponse> responses = incomeService.getIncomesByAccount(1L);

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);

        verify(incomeRepository).findByAccountId(1L);
        verify(incomeMapper).toResponse(validIncome);
    }

    // ==================== GET INCOMES BY CATEGORY TESTS ====================

    @Test
    @DisplayName("GET BY CATEGORY - Debe retornar lista de IncomeResponse filtrada por categoría")
    void getIncomesByCategory_ShouldReturnIncomeResponseList() {
        when(incomeRepository.findByCategoryId(1L)).thenReturn(Collections.singletonList(validIncome));
        when(incomeMapper.toResponse(validIncome)).thenReturn(incomeResponse);

        List<IncomeResponse> responses = incomeService.getIncomesByCategory(1L);

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);

        verify(incomeRepository).findByCategoryId(1L);
        verify(incomeMapper).toResponse(validIncome);
    }

    // ==================== GET INCOMES BY DATE RANGE TESTS ====================

    @Test
    @DisplayName("GET BY DATE RANGE - Debe retornar lista de IncomeResponse filtrada por fechas")
    void getIncomesByDateRange_ShouldReturnIncomeResponseList() {
        LocalDate start = LocalDate.of(2026, 10, 1);
        LocalDate end = LocalDate.of(2026, 10, 31);

        when(incomeRepository.findByDateBetween(start, end)).thenReturn(Collections.singletonList(validIncome));
        when(incomeMapper.toResponse(validIncome)).thenReturn(incomeResponse);

        List<IncomeResponse> responses = incomeService.getIncomesByDateRange(start, end);

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);

        verify(incomeRepository).findByDateBetween(start, end);
        verify(incomeMapper).toResponse(validIncome);
    }

    // ==================== UPDATE INCOME TESTS ====================

    @Test
    @DisplayName("UPDATE - Datos válidos debe actualizar y retornar IncomeResponse")
    void updateIncome_ValidData_ShouldReturnUpdatedIncomeResponse() {
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(validIncome));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(validAccount));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(validCategory));
        when(incomeRepository.save(any(Income.class))).thenReturn(validIncome);

        IncomeResponse updatedResponseDto = IncomeResponse.builder()
                .id(1L)
                .amount(new BigDecimal("600.00"))
                .description("Pago actualizado")
                .build();
        when(incomeMapper.toResponse(validIncome)).thenReturn(updatedResponseDto);

        IncomeResponse response = incomeService.updateIncome(1L, updateIncomeRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("600.00"));
        assertThat(response.getDescription()).isEqualTo("Pago actualizado");

        verify(incomeRepository).findById(1L);
        verify(accountRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(incomeRepository).save(validIncome);
        verify(incomeMapper).toResponse(validIncome);
    }

    @Test
    @DisplayName("UPDATE - Ingreso no encontrado debe lanzar RuntimeException")
    void updateIncome_IncomeNotFound_ShouldThrowRuntimeException() {
        when(incomeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incomeService.updateIncome(99L, updateIncomeRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ingreso no encontrado");

        verify(incomeRepository).findById(99L);
        verify(accountRepository, never()).findById(any());
        verify(incomeRepository, never()).save(any());
    }

    @Test
    @DisplayName("UPDATE - Cuenta bancaria no encontrada debe lanzar RuntimeException")
    void updateIncome_AccountNotFound_ShouldThrowRuntimeException() {
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(validIncome));
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incomeService.updateIncome(1L, updateIncomeRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cuenta bancaria no encontrada");

        verify(incomeRepository).findById(1L);
        verify(accountRepository).findById(1L);
        verify(categoryRepository, never()).findById(any());
        verify(incomeRepository, never()).save(any());
    }

    @Test
    @DisplayName("UPDATE - Categoría no encontrada debe lanzar RuntimeException")
    void updateIncome_CategoryNotFound_ShouldThrowRuntimeException() {
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(validIncome));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(validAccount));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incomeService.updateIncome(1L, updateIncomeRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Categoría no encontrada");

        verify(incomeRepository).findById(1L);
        verify(accountRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(incomeRepository, never()).save(any());
    }

    // ==================== DELETE INCOME TESTS ====================

    @Test
    @DisplayName("DELETE - ID existente debe eliminar el ingreso correctamente")
    void deleteIncome_ExistingId_ShouldDeleteIncome() {
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(validIncome));
        doNothing().when(incomeRepository).delete(validIncome);

        assertThatCode(() -> incomeService.deleteIncome(1L)).doesNotThrowAnyException();

        verify(incomeRepository).findById(1L);
        verify(incomeRepository).delete(validIncome);
    }

    @Test
    @DisplayName("DELETE - ID inexistente debe lanzar RuntimeException")
    void deleteIncome_NonExistingId_ShouldThrowRuntimeException() {
        when(incomeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incomeService.deleteIncome(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ingreso no encontrado");

        verify(incomeRepository).findById(99L);
        verify(incomeRepository, never()).delete((Income) any());
    }
}