package com.userManagement.businessLayer.service.impl;

import com.userManagement.businessLayer.dto.AuthDTOs.*;
import com.userManagement.businessLayer.service.AuthService;
import com.userManagement.persistenceLayer.entity.User;
import com.userManagement.persistenceLayer.entity.UserStatus;
import com.userManagement.persistenceLayer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. Verificar existencia del usuario por correo
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        // 2. Verificar estado de la cuenta (RN-003: No puede iniciar sesión si está inactivo/bloqueado)
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("La cuenta se encuentra desactivada o bloqueada");
        }

        // 3. Verificar credenciales (Contraseña)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // Opcional educativo: manejar contador de intentos fallidos si lo tienes en la entidad
            throw new RuntimeException("Credenciales inválidas");
        }

        // 4. Si todo es correcto, retornamos la respuesta sin tokens ni sesiones complejas
        return LoginResponse.builder()
                .message("Autenticación exitosa")
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .status(user.getStatus().name())
                .build();
    }
}