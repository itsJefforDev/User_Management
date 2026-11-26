package com.userManagement.businessLayer.service.impl;

import com.userManagement.businessLayer.dto.AuthDTOs.*;
import com.userManagement.businessLayer.exception.AuthException;
import com.userManagement.businessLayer.service.AuthService;
import com.userManagement.businessLayer.service.EmailService;
import com.userManagement.persistenceLayer.entity.User;
import com.userManagement.persistenceLayer.entity.UserStatus;
import com.userManagement.persistenceLayer.repository.UserRepository;
import com.userManagement.securityLayer.CredentialGenerator;
import com.userManagement.securityLayer.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 3;

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final CredentialGenerator credentialGenerator;

    // Vigencia de la contraseña temporal del recordatorio de contraseña
    @Value("${app.security.recovery-password-minutes:30}")
    private int recoveryPasswordMinutes;

    // noRollbackFor: los intentos fallidos deben persistirse aunque se lance AuthException
    @Override
    @Transactional(noRollbackFor = AuthException.class)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("INVALID_CREDENTIALS", "Credenciales inválidas", HttpStatus.UNAUTHORIZED));

        assertAccountUsable(user);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception e) {
            throw registerFailedAttempt(user);
        }

        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        // Contraseña temporal (admin o recordatorio): no se abre sesión hasta que la cambie
        if (Boolean.TRUE.equals(user.getMustChangePassword())) {
            LocalDateTime expiresAt = user.getTemporaryPasswordExpiresAt();
            if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) {
                throw new AuthException("PASSWORD_EXPIRED",
                        "Tu contraseña temporal ha vencido. Debes establecer una nueva contraseña.",
                        HttpStatus.FORBIDDEN);
            }
            return LoginResponse.builder()
                    .message("Debes cambiar tu contraseña temporal para continuar")
                    .mustChangePassword(true)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .status(user.getStatus())
                    .build();
        }

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoles().stream().map(r -> "ROLE_" + r.getName()).toList().toArray(new String[0]))
                .build();

        String jwtToken = jwtService.generateToken(userDetails);

        return LoginResponse.builder()
                .token(jwtToken)
                .message("¡Autenticación exitosa!")
                .mustChangePassword(false)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .status(user.getStatus())
                .role(user.getRoles())
                .build();
    }

    /**
     * Recordatorio de contraseña. Responde igual exista o no la cuenta (no revela correos)
     * y solo actúa sobre cuentas ACTIVE.
     */
    @Override
    @Transactional
    public void forgotPassword(String email) {
        if (email == null) return;

        userRepository.findByEmail(email.trim()).ifPresent(user -> {
            if (user.getStatus() != UserStatus.ACTIVE) return;

            String temporaryPassword = credentialGenerator.generateTemporaryPassword();
            user.setPassword(passwordEncoder.encode(temporaryPassword));
            user.setMustChangePassword(true);
            user.setTemporaryPasswordExpiresAt(LocalDateTime.now().plusMinutes(recoveryPasswordMinutes));
            user.setFailedLoginAttempts(0);
            userRepository.save(user);

            emailService.sendTemporaryPasswordEmail(user.getEmail(), temporaryPassword, recoveryPasswordMinutes);
        });
    }

    /**
     * Cambio de contraseña. La contraseña actual (aunque sea temporal y esté vencida)
     * solo sirve para autorizar este cambio; no abre sesión. Cuenta como intento
     * fallido si es incorrecta, para evitar fuerza bruta sobre contraseñas de 8 dígitos.
     */
    @Override
    @Transactional(noRollbackFor = AuthException.class)
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email == null ? "" : email.trim())
                .orElseThrow(() -> new AuthException("INVALID_CREDENTIALS", "Credenciales inválidas", HttpStatus.UNAUTHORIZED));

        assertAccountUsable(user);

        if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw registerFailedAttempt(user);
        }

        validatePasswordPolicy(newPassword);

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new AuthException("SAME_PASSWORD", "La nueva contraseña debe ser diferente a la actual.", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        user.setTemporaryPasswordExpiresAt(null);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
    }

    // ───────── Helpers ─────────

    private void assertAccountUsable(User user) {
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AuthException("ACCOUNT_LOCKED", "La cuenta está bloqueada por múltiples intentos fallidos.", HttpStatus.FORBIDDEN);
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new AuthException("ACCOUNT_INACTIVE", "La cuenta se encuentra inactiva. Actívala desde tu correo o contacta al administrador.", HttpStatus.FORBIDDEN);
        }
    }

    /** Suma un intento fallido, bloquea al llegar al máximo y devuelve la excepción a lanzar. */
    private AuthException registerFailedAttempt(User user) {
        int current = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0;
        int attempts = current + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setStatus(UserStatus.LOCKED);
            userRepository.save(user);
            return new AuthException("ACCOUNT_LOCKED",
                    "Contraseña incorrecta. Tu cuenta ha sido bloqueada tras " + MAX_FAILED_ATTEMPTS + " intentos fallidos.",
                    HttpStatus.FORBIDDEN);
        }

        userRepository.save(user);
        return new AuthException("INVALID_CREDENTIALS",
                "Credenciales inválidas. Intento " + attempts + " de " + MAX_FAILED_ATTEMPTS + ".",
                HttpStatus.UNAUTHORIZED);
    }

    /** Política (HU-06): mínimo 8 caracteres, al menos una letra y un número. */
    private void validatePasswordPolicy(String password) {
        if (password == null || password.length() < 8
                || !password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
            throw new AuthException("WEAK_PASSWORD",
                    "La nueva contraseña debe tener mínimo 8 caracteres, al menos una letra y un número.",
                    HttpStatus.BAD_REQUEST);
        }
    }
}