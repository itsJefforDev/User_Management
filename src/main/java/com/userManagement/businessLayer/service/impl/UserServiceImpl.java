package com.userManagement.businessLayer.service.impl;

import com.userManagement.businessLayer.dto.UserDTOs.*;
import com.userManagement.businessLayer.service.EmailService; // Asegúrate de tener este servicio inyectado
import com.userManagement.businessLayer.service.UserService;
import com.userManagement.persistenceLayer.entity.Role;
import com.userManagement.persistenceLayer.entity.User;
import com.userManagement.persistenceLayer.entity.UserStatus;
import com.userManagement.persistenceLayer.mapper.UserMapper;
import com.userManagement.persistenceLayer.repository.RoleRepository;
import com.userManagement.persistenceLayer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final EmailService emailService; // Servicio para enviar el correo de confirmación

    // 1. Creado por el ADMINISTRADOR (Nace ACTIVO por defecto)
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

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE) // El admin crea cuentas activas directamente
                .failedLoginAttempts(0)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);
        return userMapper != null ? userMapper.toResponse(savedUser) : mapToResponseFallback(savedUser);
    }

    // 2. NUEVO: Registro por primera vez por parte del USUARIO (Nace INACTIVE y con token de verificación)
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

        // Generar token único de verificación
        String verificationToken = UUID.randomUUID().toString();

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.INACTIVE) // Inactivo hasta que confirme su correo
                .verificationToken(verificationToken)
                .failedLoginAttempts(0)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        // Enviar el correo electrónico con el enlace de verificación
        emailService.sendVerificationEmail(savedUser.getEmail(), verificationToken);

        return userMapper != null ? userMapper.toResponse(savedUser) : mapToResponseFallback(savedUser);
    }

    // 3. NUEVO: Lógica para verificar la cuenta cuando el usuario hace clic en el enlace
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