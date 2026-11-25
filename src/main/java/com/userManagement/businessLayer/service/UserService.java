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

    // 1. Agregar la firma del autoregistro
    UserResponse registerUser(CreateUserRequest request);

    // 2. Agregar la firma de la verificación de cuenta
    String verifyAccount(String token);

    boolean existsByEmail(String email);
}