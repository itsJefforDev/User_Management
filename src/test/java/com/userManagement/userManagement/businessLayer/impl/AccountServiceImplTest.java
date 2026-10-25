package com.userManagement.userManagement.businessLayer.impl;

import com.userManagement.businessLayer.dto.AccountDTOs.*;
import com.userManagement.businessLayer.service.impl.AccountServiceImpl;
import com.userManagement.persistenceLayer.entity.Account;
import com.userManagement.persistenceLayer.entity.User;
import com.userManagement.persistenceLayer.mapper.AccountMapper;
import com.userManagement.persistenceLayer.repository.AccountRepository;
import com.userManagement.persistenceLayer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests para AccountServiceImpl
 *
 * OBJETIVO: Probar la lógica de negocio del servicio de cuentas bancarias de forma aislada
 * - Cobertura del 100% de los métodos y caminos alternativos (creación, consultas, actualización, desactivación, saldo)
 * - Sin base de datos ni Spring Context
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService - Unit Tests")
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountServiceImpl accountService;

    private User validUser;
    private Account validAccount;
    private CreateAccountRequest createAccountRequest;
    private UpdateAccountRequest updateAccountRequest;
    private AccountResponse accountResponse;

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
                .type("BANK")
                .balance(new BigDecimal("1500.00"))
                .active(true)
                .user(validUser)
                .build();

        createAccountRequest = new CreateAccountRequest();
        createAccountRequest.setUserId(1L);
        createAccountRequest.setName("Cuenta de Ahorros");
        createAccountRequest.setType("BANK");
        createAccountRequest.setInitialBalance(new BigDecimal("1500.00"));

        updateAccountRequest = new UpdateAccountRequest();
        updateAccountRequest.setName("Cuenta Actualizada");
        updateAccountRequest.setType("DIGITAL_WALLET");

        accountResponse = new AccountResponse();
        accountResponse.setId(1L);
        accountResponse.setName("Cuenta de Ahorros");
        accountResponse.setType("BANK");
        accountResponse.setBalance(new BigDecimal("1500.00"));
        accountResponse.setActive(true);
    }

    // ==================== CREATE ACCOUNT TESTS ====================

    @Test
    @DisplayName("CREATE - Usuario existente y datos válidos debe guardar y retornar AccountResponse")
    void createAccount_ValidData_ShouldReturnAccountResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        when(accountMapper.toEntity(any(CreateAccountRequest.class), eq(validUser))).thenReturn(validAccount);
        when(accountRepository.save(any(Account.class))).thenReturn(validAccount);
        when(accountMapper.toResponse(validAccount)).thenReturn(accountResponse);

        AccountResponse response = accountService.createAccount(createAccountRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Cuenta de Ahorros");

        verify(userRepository).findById(1L);
        verify(accountMapper).toEntity(createAccountRequest, validUser);
        verify(accountRepository).save(validAccount);
        verify(accountMapper).toResponse(validAccount);
    }

    @Test
    @DisplayName("CREATE - Usuario no encontrado debe lanzar RuntimeException")
    void createAccount_UserNotFound_ShouldThrowRuntimeException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        createAccountRequest.setUserId(99L);

        assertThatThrownBy(() -> accountService.createAccount(createAccountRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado con ID: 99");

        verify(userRepository).findById(99L);
        verify(accountRepository, never()).save(any(Account.class));
    }

    // ==================== GET ACCOUNT BY ID TESTS ====================

    @Test
    @DisplayName("GET BY ID - ID existente debe retornar AccountResponse")
    void getAccountById_ExistingId_ShouldReturnAccountResponse() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(validAccount));
        when(accountMapper.toResponse(validAccount)).thenReturn(accountResponse);

        AccountResponse response = accountService.getAccountById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);

        verify(accountRepository).findById(1L);
        verify(accountMapper).toResponse(validAccount);
    }

    @Test
    @DisplayName("GET BY ID - ID inexistente debe lanzar RuntimeException")
    void getAccountById_NonExistingId_ShouldThrowRuntimeException() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccountById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cuenta bancaria no encontrada con ID: 99");

        verify(accountRepository).findById(99L);
        verify(accountMapper, never()).toResponse(any());
    }

    // ==================== GET ACCOUNTS BY USER TESTS ====================

    @Test
    @DisplayName("GET BY USER - Debe retornar lista de AccountResponse")
    void getAccountsByUser_ShouldReturnAccountResponseList() {
        when(accountRepository.findByUserId(1L)).thenReturn(Collections.singletonList(validAccount));
        when(accountMapper.toResponse(validAccount)).thenReturn(accountResponse);

        List<AccountResponse> responses = accountService.getAccountsByUser(1L);

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("Cuenta de Ahorros");

        verify(accountRepository).findByUserId(1L);
        verify(accountMapper).toResponse(validAccount);
    }

    // ==================== UPDATE ACCOUNT TESTS ====================

    @Test
    @DisplayName("UPDATE - Datos válidos debe actualizar y retornar AccountResponse")
    void updateAccount_ValidData_ShouldReturnUpdatedAccountResponse() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(validAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(validAccount);

        AccountResponse updatedResponse = new AccountResponse();
        updatedResponse.setId(1L);
        updatedResponse.setName("Cuenta Actualizada");
        updatedResponse.setType("DIGITAL_WALLET");

        when(accountMapper.toResponse(validAccount)).thenReturn(updatedResponse);

        AccountResponse response = accountService.updateAccount(1L, updateAccountRequest);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Cuenta Actualizada");
        assertThat(response.getType()).isEqualTo("DIGITAL_WALLET");

        verify(accountRepository).findById(1L);
        verify(accountRepository).save(validAccount);
        verify(accountMapper).toResponse(validAccount);
    }

    @Test
    @DisplayName("UPDATE - ID inexistente debe lanzar RuntimeException")
    void updateAccount_NonExistingId_ShouldThrowRuntimeException() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.updateAccount(99L, updateAccountRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cuenta bancaria no encontrada con ID: 99");

        verify(accountRepository).findById(99L);
        verify(accountRepository, never()).save(any(Account.class));
    }

    // ==================== DEACTIVATE ACCOUNT TESTS ====================

    @Test
    @DisplayName("DEACTIVATE - ID existente debe marcar cuenta como inactiva (active = false) y guardar")
    void deactivateAccount_ExistingId_ShouldSetAccountInactive() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(validAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(validAccount);

        assertThatCode(() -> accountService.deactivateAccount(1L)).doesNotThrowAnyException();

        assertThat(validAccount.getActive()).isFalse();
        verify(accountRepository).findById(1L);
        verify(accountRepository).save(validAccount);
    }

    @Test
    @DisplayName("DEACTIVATE - ID inexistente debe lanzar RuntimeException")
    void deactivateAccount_NonExistingId_ShouldThrowRuntimeException() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.deactivateAccount(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cuenta bancaria no encontrada con ID: 99");

        verify(accountRepository).findById(99L);
        verify(accountRepository, never()).save(any(Account.class));
    }

    // ==================== GET ACCOUNT BALANCE TESTS ====================

    @Test
    @DisplayName("GET BALANCE - ID existente debe retornar el saldo en BigDecimal")
    void getAccountBalance_ExistingId_ShouldReturnBalance() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(validAccount));

        BigDecimal balance = accountService.getAccountBalance(1L);

        assertThat(balance).isNotNull();
        assertThat(balance).isEqualByComparingTo(new BigDecimal("1500.00"));

        verify(accountRepository).findById(1L);
    }

    @Test
    @DisplayName("GET BALANCE - ID inexistente debe lanzar RuntimeException")
    void getAccountBalance_NonExistingId_ShouldThrowRuntimeException() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccountBalance(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cuenta bancaria no encontrada con ID: 99");

        verify(accountRepository).findById(99L);
    }
}