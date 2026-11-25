package com.userManagement.businessLayer.service.impl;

import com.userManagement.businessLayer.dto.AuthDTOs.*;
import com.userManagement.businessLayer.service.AuthService;
import com.userManagement.persistenceLayer.entity.User;
import com.userManagement.persistenceLayer.entity.UserStatus;
import com.userManagement.persistenceLayer.repository.UserRepository;
import com.userManagement.securityLayer.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new RuntimeException("La cuenta está bloqueada por múltiples intentos fallidos.");
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new RuntimeException("La cuenta se encuentra inactiva.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            // Validamos por seguridad si es null para evitar NullPointerException
            int currentAttempts = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0;
            int attempts = currentAttempts + 1;

            user.setFailedLoginAttempts(attempts);

            if (attempts >= 3) {
                user.setStatus(UserStatus.LOCKED);
                userRepository.save(user);
                throw new RuntimeException("Contraseña incorrecta. Tu cuenta ha sido bloqueada tras 3 intentos fallidos.");
            }

            userRepository.save(user);
            throw new RuntimeException("Credenciales inválidas. Intento " + attempts + " de 3.");
        }

        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoles().stream().map(r -> "ROLE_" + r.getName()).toList().toArray(new String[0]))
                .build();

        String jwtToken = jwtService.generateToken(userDetails);

        return LoginResponse.builder()
                .token(jwtToken)
                .message("¡Autenticación exitosa!")
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .status(user.getStatus()) // O .status(user.getStatus().name()) dependiendo de si tu DTO espera un Enum o un String
                .role(user.getRoles())
                .build();
    }
}