package com.userManagement.businessLayer.service;

import com.userManagement.businessLayer.dto.AuthDTOs.*;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    /** Envía al correo una contraseña temporal de 8 dígitos (recordatorio de contraseña). */
    void forgotPassword(String email);

    /** Cambia la contraseña validando la actual (también la temporal, aunque esté vencida). */
    void changePassword(String email, String currentPassword, String newPassword);
}