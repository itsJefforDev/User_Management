package com.userManagement.businessLayer.service.impl;

import com.userManagement.businessLayer.dto.UserDTOs.*;
import com.userManagement.businessLayer.exception.AuthException;
import com.userManagement.businessLayer.service.EmailService;
import com.userManagement.businessLayer.service.UserService;
import com.userManagement.persistenceLayer.entity.Role;
import com.userManagement.persistenceLayer.entity.User;
import com.userManagement.persistenceLayer.entity.UserStatus;
import com.userManagement.persistenceLayer.mapper.UserMapper;
import com.userManagement.persistenceLayer.repository.RoleRepository;
import com.userManagement.persistenceLayer.repository.UserRepository;
import com.userManagement.securityLayer.CredentialGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final int DEFAULT_TEMP_PASSWORD_HOURS = 24;
    private static final int MAX_TEMP_PASSWORD_HOURS = 720; // 30 días
    private static final int MAX_CODE_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final EmailService emailService; // Servicio para enviar los correos
    private final CredentialGenerator credentialGenerator;

    // Vigencia del código de 4 dígitos del auto-registro
    @Value("${app.security.verification-code-minutes:15}")
    private int verificationCodeMinutes;

    // 1. Creado por el ADMINISTRADOR: nace INACTIVO con contraseña temporal
    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya se encuentra registrado");
        }

        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + roleName));
                roles.add(role);
            }
        } else {
            Role defaultRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Rol por defecto ROLE_USER no encontrado"));
            roles.add(defaultRole);
        }

        String temporaryPassword = resolveTemporaryPassword(request.getPassword());
        int hours = resolveTemporaryHours(request.getTemporaryPasswordHours());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(temporaryPassword))
                .status(UserStatus.INACTIVE) // Se activa por correo o manualmente desde la edición
                .failedLoginAttempts(0)
                .mustChangePassword(true)    // Obliga a cambiar la contraseña en el primer ingreso
                .temporaryPasswordExpiresAt(LocalDateTime.now().plusHours(hours))
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);
        return userMapper != null ? userMapper.toResponse(savedUser) : mapToResponseFallback(savedUser);
    }

    // 2. Registro por primera vez por parte del USUARIO (nace INACTIVE y recibe un código de 4 dígitos)
    @Override
    @Transactional
    public UserResponse registerUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya se encuentra registrado");
        }

        // Asignar rol por defecto para autoregistro
        Set<Role> roles = new HashSet<>();
        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Rol por defecto ROLE_USER no encontrado"));
        roles.add(defaultRole);

        String code = credentialGenerator.generateVerificationCode();

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.INACTIVE) // Inactivo hasta que digite el código
                .verificationCode(code)
                .verificationCodeExpiresAt(LocalDateTime.now().plusMinutes(verificationCodeMinutes))
                .verificationCodeAttempts(0)
                .failedLoginAttempts(0)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        emailService.sendVerificationCodeEmail(savedUser.getEmail(), code, verificationCodeMinutes);

        return userMapper != null ? userMapper.toResponse(savedUser) : mapToResponseFallback(savedUser);
    }

    // 3. Activación por ENLACE (flujo del administrador): se mantiene el formato original
    @Override
    @Transactional
    public String verifyAccount(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Token de verificación inválido o expirado."));

        user.setStatus(UserStatus.ACTIVE); // Activamos la cuenta
        user.setVerificationToken(null);  // Limpiamos el token para que no se pueda reutilizar
        userRepository.save(user);

        return "¡Cuenta verificada exitosamente! Ya puedes iniciar sesión.";
    }

    // 4. Activación por CÓDIGO de 4 dígitos (flujo de auto-registro)
    // noRollbackFor: el conteo de intentos debe persistirse aunque se lance la excepción
    @Override
    @Transactional(noRollbackFor = AuthException.class)
    public String verifyCode(String email, String code) {
        User user = userRepository.findByEmail(email == null ? "" : email.trim())
                .orElseThrow(this::invalidCode);

        if (user.getStatus() == UserStatus.ACTIVE) {
            return "La cuenta ya se encuentra activa.";
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AuthException("ACCOUNT_LOCKED", "La cuenta está bloqueada.", HttpStatus.FORBIDDEN);
        }
        if (user.getVerificationCode() == null) {
            throw invalidCode();
        }

        int attempts = user.getVerificationCodeAttempts() != null ? user.getVerificationCodeAttempts() : 0;
        if (attempts >= MAX_CODE_ATTEMPTS) {
            throw new AuthException("CODE_ATTEMPTS_EXCEEDED",
                    "Superaste los intentos permitidos. Solicita un nuevo código.", HttpStatus.TOO_MANY_REQUESTS);
        }
        if (user.getVerificationCodeExpiresAt() == null || user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AuthException("CODE_EXPIRED", "El código ha expirado. Solicita uno nuevo.", HttpStatus.BAD_REQUEST);
        }
        if (!user.getVerificationCode().equals(code == null ? "" : code.trim())) {
            user.setVerificationCodeAttempts(attempts + 1);
            userRepository.save(user);
            throw new AuthException("CODE_INVALID",
                    "Código incorrecto. Intentos restantes: " + (MAX_CODE_ATTEMPTS - attempts - 1) + ".",
                    HttpStatus.BAD_REQUEST);
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        user.setVerificationCodeAttempts(0);
        userRepository.save(user);

        return "¡Cuenta verificada exitosamente! Ya puedes iniciar sesión.";
    }

    // Reenvío del código: solo para cuentas auto-registradas pendientes (las del admin no tienen código)
    @Override
    @Transactional
    public void resendVerificationCode(String email) {
        if (email == null) return;

        userRepository.findByEmail(email.trim()).ifPresent(user -> {
            if (user.getStatus() != UserStatus.INACTIVE || user.getVerificationCode() == null) return;

            String code = credentialGenerator.generateVerificationCode();
            user.setVerificationCode(code);
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(verificationCodeMinutes));
            user.setVerificationCodeAttempts(0);
            userRepository.save(user);

            emailService.sendVerificationCodeEmail(user.getEmail(), code, verificationCodeMinutes);
        });
    }

    // Administrador: genera una contraseña temporal aleatoria de 8 dígitos
    @Override
    public TemporaryPasswordResponse generateTemporaryPassword() {
        return TemporaryPasswordResponse.builder()
                .password(credentialGenerator.generateTemporaryPassword())
                .build();
    }

    // Administrador: envía el correo de activación por ENLACE a una cuenta inactiva
    @Override
    @Transactional
    public void sendActivationEmail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new IllegalArgumentException("La cuenta ya se encuentra activa.");
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new IllegalArgumentException("La cuenta está bloqueada. Cambia su estado antes de enviar la activación.");
        }

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        return userMapper != null ? userMapper.toResponse(user) : mapToResponseFallback(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> userMapper != null ? userMapper.toResponse(user) : mapToResponseFallback(user))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
            // Activación manual: no requiere pasar por el correo, se limpian los datos pendientes
            if (request.getStatus() == UserStatus.ACTIVE) {
                user.setVerificationToken(null);
                user.setVerificationCode(null);
                user.setVerificationCodeExpiresAt(null);
                user.setVerificationCodeAttempts(0);
                user.setFailedLoginAttempts(0);
            }
        }
        if (request.getRoles() != null) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : request.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + roleName));
                roles.add(role);
            }
            user.setRoles(roles);
        }
        // Nueva contraseña temporal asignada por el administrador (obliga a cambiarla)
        if (StringUtils.hasText(request.getPassword())) {
            String temporaryPassword = resolveTemporaryPassword(request.getPassword());
            user.setPassword(passwordEncoder.encode(temporaryPassword));
            user.setMustChangePassword(true);
            user.setTemporaryPasswordExpiresAt(
                    LocalDateTime.now().plusHours(resolveTemporaryHours(request.getTemporaryPasswordHours())));
            user.setFailedLoginAttempts(0);
        }

        User updatedUser = userRepository.save(user);
        return userMapper != null ? userMapper.toResponse(updatedUser) : mapToResponseFallback(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void handleFailedLogin(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            int attempts = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() + 1 : 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= 3) {
                user.setStatus(UserStatus.LOCKED);
            }
            userRepository.save(user);
        });
    }

    @Transactional
    @Override
    public void resetFailedLoginAttempts(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        });
    }

    // ───────── Helpers ─────────

    /** Usa la contraseña temporal enviada (debe tener 8 dígitos) o genera una aleatoria. */
    private String resolveTemporaryPassword(String provided) {
        if (!StringUtils.hasText(provided)) {
            return credentialGenerator.generateTemporaryPassword();
        }
        if (!provided.matches("\\d{8}")) {
            throw new IllegalArgumentException("La contraseña temporal debe tener 8 dígitos.");
        }
        return provided;
    }

    /** Vigencia en horas de la contraseña temporal (por defecto 24, máximo 720). */
    private int resolveTemporaryHours(Integer hours) {
        if (hours == null) return DEFAULT_TEMP_PASSWORD_HOURS;
        if (hours < 1 || hours > MAX_TEMP_PASSWORD_HOURS) {
            throw new IllegalArgumentException("La vigencia debe estar entre 1 y " + MAX_TEMP_PASSWORD_HOURS + " horas.");
        }
        return hours;
    }

    private AuthException invalidCode() {
        return new AuthException("CODE_INVALID", "Código inválido o expirado.", HttpStatus.BAD_REQUEST);
    }

    private UserResponse mapToResponseFallback(User user) {
        Set<String> roleNames = user.getRoles() != null ?
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()) : new HashSet<>();
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .status(user.getStatus())
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .roles(roleNames)
                .mustChangePassword(user.getMustChangePassword())
                .temporaryPasswordExpiresAt(user.getTemporaryPasswordExpiresAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}