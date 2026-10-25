package com.userManagement.userManagement.businessLayer.impl;

import com.userManagement.businessLayer.dto.SavingDTOs.*;
import com.userManagement.businessLayer.service.impl.SavingServiceImpl;
import com.userManagement.persistenceLayer.entity.Account;
import com.userManagement.persistenceLayer.entity.SavingContribution;
import com.userManagement.persistenceLayer.entity.SavingGoal;
import com.userManagement.persistenceLayer.entity.User;
import com.userManagement.persistenceLayer.mapper.SavingMapper;
import com.userManagement.persistenceLayer.repository.AccountRepository;
import com.userManagement.persistenceLayer.repository.SavingContributionRepository;
import com.userManagement.persistenceLayer.repository.SavingGoalRepository;
import com.userManagement.persistenceLayer.repository.UserRepository;
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
 * Unit Tests para SavingServiceImpl
 *
 * OBJETIVO: Probar la lógica de negocio del servicio de ahorro de forma aislada
 * - Cobertura del 100% de los métodos y caminos alternativos (metas, aportes, validaciones de usuario y cuenta, estados)
 * - Sin base de datos ni Spring Context
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SavingService - Unit Tests")
class SavingServiceImplTest {

    @Mock
    private SavingGoalRepository savingGoalRepository;

    @Mock
    private SavingContributionRepository contributionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private SavingMapper savingMapper;

    @InjectMocks
    private SavingServiceImpl savingService;

    private User validUser;
    private Account validAccount;
    private SavingGoal validSavingGoal;
    private SavingContribution validContribution;

    private CreateSavingGoalRequest createSavingGoalRequest;
    private UpdateSavingGoalRequest updateSavingGoalRequest;
    private CreateContributionRequest createContributionRequest;

    private SavingGoalResponse savingGoalResponse;
    private SavingContributionResponse savingContributionResponse;

    @BeforeEach
    void setUp() {
        validUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@email.com")
                .build();

        validAccount = Account.builder()
                .id(1L)
                .name("Cuenta de Ahorros")
                .balance(new BigDecimal("1000.00"))
                .build();

        validSavingGoal = SavingGoal.builder()
                .id(1L)
                .name("Vacaciones")
                .description("Ahorro para viaje")
                .targetAmount(new BigDecimal("500.00"))
                .currentAmount(BigDecimal.ZERO)
                .targetDate(LocalDate.of(2026, 12, 31))
                .status("IN_PROGRESS")
                .user(validUser)
                .build();

        validContribution = SavingContribution.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.now())
                .notes("Primer aporte")
                .savingGoal(validSavingGoal)
                .account(validAccount)
                .build();

        createSavingGoalRequest = new CreateSavingGoalRequest();
        createSavingGoalRequest.setName("Vacaciones");
        createSavingGoalRequest.setDescription("Ahorro para viaje");
        createSavingGoalRequest.setTargetAmount(new BigDecimal("500.00"));
        createSavingGoalRequest.setTargetDate(LocalDate.of(2026, 12, 31));
        createSavingGoalRequest.setUserId(1L);

        updateSavingGoalRequest = new UpdateSavingGoalRequest();
        updateSavingGoalRequest.setName("Vacaciones Actualizadas");
        updateSavingGoalRequest.setDescription("Nueva descripción");
        updateSavingGoalRequest.setTargetAmount(new BigDecimal("600.00"));
        updateSavingGoalRequest.setTargetDate(LocalDate.of(2026, 12, 31));
        updateSavingGoalRequest.setStatus("IN_PROGRESS");

        createContributionRequest = new CreateContributionRequest();
        createContributionRequest.setAmount(new BigDecimal("100.00"));
        createContributionRequest.setDate(LocalDate.now());
        createContributionRequest.setNotes("Primer aporte");
        createContributionRequest.setSavingGoalId(1L);
        createContributionRequest.setAccountId(1L);

        savingGoalResponse = SavingGoalResponse.builder()
                .id(1L)
                .name("Vacaciones")
                .targetAmount(new BigDecimal("500.00"))
                .currentAmount(BigDecimal.ZERO)
                .status("IN_PROGRESS")
                .userId(1L)
                .build();

        savingContributionResponse = SavingContributionResponse.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .savingGoalId(1L)
                .accountName("Cuenta de Ahorros")
                .build();
    }

    // ==================== CREATE SAVING GOAL TESTS ====================

    @Test
    @DisplayName("CREATE GOAL - Usuario existente y datos válidos debe guardar y retornar SavingGoalResponse")
    void createSavingGoal_ValidData_ShouldReturnGoalResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        when(savingGoalRepository.save(any(SavingGoal.class))).thenReturn(validSavingGoal);
        when(savingMapper.toGoalResponse(validSavingGoal)).thenReturn(savingGoalResponse);

        SavingGoalResponse response = savingService.createSavingGoal(createSavingGoalRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Vacaciones");

        verify(userRepository).findById(1L);
        verify(savingGoalRepository).save(any(SavingGoal.class));
        verify(savingMapper).toGoalResponse(validSavingGoal);
    }

    @Test
    @DisplayName("CREATE GOAL - Usuario no encontrado debe lanzar RuntimeException")
    void createSavingGoal_UserNotFound_ShouldThrowRuntimeException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        createSavingGoalRequest.setUserId(99L);

        assertThatThrownBy(() -> savingService.createSavingGoal(createSavingGoalRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado con ID: 99");

        verify(userRepository).findById(99L);
        verify(savingGoalRepository, never()).save(any(SavingGoal.class));
    }

    // ==================== GET SAVING GOAL BY ID TESTS ====================

    @Test
    @DisplayName("GET GOAL BY ID - ID existente debe retornar SavingGoalResponse")
    void getSavingGoalById_ExistingId_ShouldReturnGoalResponse() {
        when(savingGoalRepository.findById(1L)).thenReturn(Optional.of(validSavingGoal));
        when(savingMapper.toGoalResponse(validSavingGoal)).thenReturn(savingGoalResponse);

        SavingGoalResponse response = savingService.getSavingGoalById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);

        verify(savingGoalRepository).findById(1L);
        verify(savingMapper).toGoalResponse(validSavingGoal);
    }

    @Test
    @DisplayName("GET GOAL BY ID - ID inexistente debe lanzar RuntimeException")
    void getSavingGoalById_NonExistingId_ShouldThrowRuntimeException() {
        when(savingGoalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> savingService.getSavingGoalById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Meta de ahorro no encontrada con ID: 99");

        verify(savingGoalRepository).findById(99L);
        verify(savingMapper, never()).toGoalResponse(any());
    }

    // ==================== GET SAVING GOALS BY USER TESTS ====================

    @Test
    @DisplayName("GET GOALS BY USER - Debe retornar lista de SavingGoalResponse")
    void getSavingGoalsByUser_ShouldReturnGoalResponseList() {
        when(savingGoalRepository.findByUserId(1L)).thenReturn(Collections.singletonList(validSavingGoal));
        when(savingMapper.toGoalResponse(validSavingGoal)).thenReturn(savingGoalResponse);

        List<SavingGoalResponse> responses = savingService.getSavingGoalsByUser(1L);

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("Vacaciones");

        verify(savingGoalRepository).findByUserId(1L);
        verify(savingMapper).toGoalResponse(validSavingGoal);
    }

    // ==================== UPDATE SAVING GOAL TESTS ====================

    @Test
    @DisplayName("UPDATE GOAL - Datos válidos debe actualizar y retornar SavingGoalResponse")
    void updateSavingGoal_ValidData_ShouldReturnUpdatedGoalResponse() {
        when(savingGoalRepository.findById(1L)).thenReturn(Optional.of(validSavingGoal));
        when(savingGoalRepository.save(any(SavingGoal.class))).thenReturn(validSavingGoal);

        SavingGoalResponse updatedResponse = SavingGoalResponse.builder()
                .id(1L)
                .name("Vacaciones Actualizadas")
                .status("IN_PROGRESS")
                .build();

        when(savingMapper.toGoalResponse(validSavingGoal)).thenReturn(updatedResponse);

        SavingGoalResponse response = savingService.updateSavingGoal(1L, updateSavingGoalRequest);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Vacaciones Actualizadas");

        verify(savingGoalRepository).findById(1L);
        verify(savingGoalRepository).save(validSavingGoal);
        verify(savingMapper).toGoalResponse(validSavingGoal);
    }

    @Test
    @DisplayName("UPDATE GOAL - Con status nulo no debe sobrescribir el status y actualizar con éxito")
    void updateSavingGoal_NullStatus_ShouldNotUpdateStatus() {
        updateSavingGoalRequest.setStatus(null);
        when(savingGoalRepository.findById(1L)).thenReturn(Optional.of(validSavingGoal));
        when(savingGoalRepository.save(any(SavingGoal.class))).thenReturn(validSavingGoal);
        when(savingMapper.toGoalResponse(validSavingGoal)).thenReturn(savingGoalResponse);

        SavingGoalResponse response = savingService.updateSavingGoal(1L, updateSavingGoalRequest);

        assertThat(response).isNotNull();
        verify(savingGoalRepository).findById(1L);
        verify(savingGoalRepository).save(validSavingGoal);
    }

    @Test
    @DisplayName("UPDATE GOAL - ID inexistente debe lanzar RuntimeException")
    void updateSavingGoal_NonExistingId_ShouldThrowRuntimeException() {
        when(savingGoalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> savingService.updateSavingGoal(99L, updateSavingGoalRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Meta de ahorro no encontrada con ID: 99");

        verify(savingGoalRepository).findById(99L);
        verify(savingGoalRepository, never()).save(any(SavingGoal.class));
    }

    // ==================== DELETE SAVING GOAL TESTS ====================

    @Test
    @DisplayName("DELETE GOAL - ID existente debe eliminar la meta de ahorro")
    void deleteSavingGoal_ExistingId_ShouldDeleteGoal() {
        when(savingGoalRepository.existsById(1L)).thenReturn(true);
        doNothing().when(savingGoalRepository).deleteById(1L);

        assertThatCode(() -> savingService.deleteSavingGoal(1L)).doesNotThrowAnyException();

        verify(savingGoalRepository).existsById(1L);
        verify(savingGoalRepository).deleteById(1L);
    }

    @Test
    @DisplayName("DELETE GOAL - ID inexistente debe lanzar RuntimeException")
    void deleteSavingGoal_NonExistingId_ShouldThrowRuntimeException() {
        when(savingGoalRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> savingService.deleteSavingGoal(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Meta de ahorro no encontrada con ID: 99");

        verify(savingGoalRepository).existsById(99L);
        verify(savingGoalRepository, never()).deleteById(any());
    }

    // ==================== ADD CONTRIBUTION TESTS ====================

    @Test
    @DisplayName("ADD CONTRIBUTION - Meta y cuenta existentes deben guardar aporte y retornar response")
    void addContribution_ValidDataWithAccount_ShouldReturnContributionResponse() {
        when(savingGoalRepository.findById(1L)).thenReturn(Optional.of(validSavingGoal));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(validAccount));
        when(savingGoalRepository.save(any(SavingGoal.class))).thenReturn(validSavingGoal);
        when(contributionRepository.save(any(SavingContribution.class))).thenReturn(validContribution);
        when(savingMapper.toContributionResponse(validContribution)).thenReturn(savingContributionResponse);

        SavingContributionResponse response = savingService.addContribution(createContributionRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));

        verify(savingGoalRepository).findById(1L);
        verify(accountRepository).findById(1L);
        verify(savingGoalRepository).save(validSavingGoal);
        verify(contributionRepository).save(any(SavingContribution.class));
        verify(savingMapper).toContributionResponse(validContribution);
    }

    @Test
    @DisplayName("ADD CONTRIBUTION - Sin cuenta asociada (accountId nulo) debe guardar aporte exitosamente")
    void addContribution_WithoutAccount_ShouldReturnContributionResponse() {
        createContributionRequest.setAccountId(null);
        when(savingGoalRepository.findById(1L)).thenReturn(Optional.of(validSavingGoal));
        when(savingGoalRepository.save(any(SavingGoal.class))).thenReturn(validSavingGoal);
        when(contributionRepository.save(any(SavingContribution.class))).thenReturn(validContribution);
        when(savingMapper.toContributionResponse(validContribution)).thenReturn(savingContributionResponse);

        SavingContributionResponse response = savingService.addContribution(createContributionRequest);

        assertThat(response).isNotNull();
        verify(savingGoalRepository).findById(1L);
        verify(accountRepository, never()).findById(any());
        verify(savingGoalRepository).save(validSavingGoal);
        verify(contributionRepository).save(any(SavingContribution.class));
    }

    @Test
    @DisplayName("ADD CONTRIBUTION - Meta completada o superada debe cambiar el estado a COMPLETED")
    void addContribution_GoalCompleted_ShouldUpdateStatusToCompleted() {
        createContributionRequest.setAmount(new BigDecimal("500.00")); // Igual al targetAmount
        when(savingGoalRepository.findById(1L)).thenReturn(Optional.of(validSavingGoal));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(validAccount));
        when(savingGoalRepository.save(any(SavingGoal.class))).thenReturn(validSavingGoal);
        when(contributionRepository.save(any(SavingContribution.class))).thenReturn(validContribution);
        when(savingMapper.toContributionResponse(validContribution)).thenReturn(savingContributionResponse);

        savingService.addContribution(createContributionRequest);

        assertThat(validSavingGoal.getStatus()).isEqualTo("COMPLETED");
        verify(savingGoalRepository).save(validSavingGoal);
    }

    @Test
    @DisplayName("ADD CONTRIBUTION - Meta de ahorro no encontrada debe lanzar RuntimeException")
    void addContribution_GoalNotFound_ShouldThrowRuntimeException() {
        when(savingGoalRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> savingService.addContribution(createContributionRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Meta de ahorro no encontrada");

        verify(savingGoalRepository).findById(1L);
        verify(contributionRepository, never()).save(any());
    }

    @Test
    @DisplayName("ADD CONTRIBUTION - Cuenta bancaria no encontrada debe lanzar RuntimeException")
    void addContribution_AccountNotFound_ShouldThrowRuntimeException() {
        when(savingGoalRepository.findById(1L)).thenReturn(Optional.of(validSavingGoal));
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> savingService.addContribution(createContributionRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cuenta bancaria no encontrada");

        verify(accountRepository).findById(1L);
        verify(contributionRepository, never()).save(any());
    }

    // ==================== GET CONTRIBUTIONS BY GOAL TESTS ====================

    @Test
    @DisplayName("GET CONTRIBUTIONS BY GOAL - Debe retornar lista de SavingContributionResponse")
    void getContributionsByGoal_ShouldReturnContributionResponseList() {
        when(contributionRepository.findBySavingGoalId(1L)).thenReturn(Collections.singletonList(validContribution));
        when(savingMapper.toContributionResponse(validContribution)).thenReturn(savingContributionResponse);

        List<SavingContributionResponse> responses = savingService.getContributionsByGoal(1L);

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));

        verify(contributionRepository).findBySavingGoalId(1L);
        verify(savingMapper).toContributionResponse(validContribution);
    }
}