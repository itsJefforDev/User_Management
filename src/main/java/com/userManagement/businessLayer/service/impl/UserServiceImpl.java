package com.userManagement.businessLayer.service.impl;

import com.userManagement.businessLayer.dto.UserDTOs.*;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

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
            // Asignar rol por defecto si no se especifican roles
            Role defaultRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Rol por defecto ROLE_USER no encontrado"));
            roles.add(defaultRole);
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .failedLoginAttempts(0)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);
        return userMapper != null ? userMapper.toResponse(savedUser) : mapToResponseFallback(savedUser);
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
}