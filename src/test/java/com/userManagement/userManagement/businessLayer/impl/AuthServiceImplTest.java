package com.userManagement.userManagement.businessLayer.impl;

import com.userManagement.businessLayer.dto.AuthDTOs.LoginRequest;
import com.userManagement.businessLayer.dto.AuthDTOs.LoginResponse;
import com.userManagement.businessLayer.service.impl.AuthServiceImpl;
import com.userManagement.persistenceLayer.entity.User;
import com.userManagement.persistenceLayer.entity.UserStatus;
import com.userManagement.persistenceLayer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit Tests para AuthServiceImpl
 *
 * OBJETIVO: Probar la lógica de negocio de autenticación de forma aislada
 * - Cobertura del 100% de los métodos y caminos alternativos (login exitoso, usuario no encontrado, cuenta inactiva, credenciales inválidas)
 * - Sin base de datos ni Spring Context
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User activeUser;
    private User inactiveUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@email.com")
                .password("encodedPassword123")
                .status(UserStatus.ACTIVE)
                .build();

        inactiveUser = User.builder()
                .id(2L)
                .name("Jane Doe")
                .email("jane.doe@email.com")
                .password("encodedPassword123")
                .status(UserStatus.INACTIVE) // O INACTIVE / BLOCKED según tu enumeración
                .build();

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john.doe@email.com");
        loginRequest.setPassword("password123");
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("LOGIN - Credenciales válidas y usuario activo debe retornar LoginResponse exitoso")
    void login_ValidCredentials_ShouldReturnLoginResponse() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), activeUser.getPassword())).thenReturn(true);

        LoginResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Autenticación exitosa");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("john.doe@email.com");
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getStatus()).isEqualTo("ACTIVE");

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), activeUser.getPassword());
    }

    @Test
    @DisplayName("LOGIN - Correo no registrado debe lanzar RuntimeException de credenciales inválidas")
    void login_UserNotFound_ShouldThrowRuntimeException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Credenciales inválidas");

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("LOGIN - Usuario inactivo o bloqueado debe lanzar RuntimeException de cuenta desactivada")
    void login_InactiveUser_ShouldThrowRuntimeException() {
        loginRequest.setEmail("jane.doe@email.com");
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(inactiveUser));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La cuenta se encuentra desactivada o bloqueada");

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("LOGIN - Contraseña incorrecta debe lanzar RuntimeException de credenciales inválidas")
    void login_InvalidPassword_ShouldThrowRuntimeException() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), activeUser.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Credenciales inválidas");

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), activeUser.getPassword());
    }
}