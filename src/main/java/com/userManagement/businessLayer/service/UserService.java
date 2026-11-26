package com.userManagement.businessLayer.service;

import com.userManagement.businessLayer.dto.UserDTOs.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    UserResponse updateUser(Long id, UpdateUserRequest request);
    void deleteUser(Long id);

    @Transactional
    void handleFailedLogin(String email);

    @Transactional
    void resetFailedLoginAttempts(String email);

    // Auto-registro (queda INACTIVE y recibe un código de 4 dígitos)
    UserResponse registerUser(CreateUserRequest request);

    // Activación por enlace (flujo del administrador)
    String verifyAccount(String token);

    // Activación por código de 4 dígitos (flujo de auto-registro)
    String verifyCode(String email, String code);

    void resendVerificationCode(String email);

    boolean existsByEmail(String email);

    // Administrador: contraseña temporal aleatoria y envío del correo de activación
    TemporaryPasswordResponse generateTemporaryPassword();

    void sendActivationEmail(Long id);
}