package com.userManagement.presentationLayer.controller;

import com.userManagement.businessLayer.dto.AuthDTOs.LoginRequest;
import com.userManagement.businessLayer.dto.AuthDTOs.LoginResponse; // O AuthResponse según cómo lo hayas nombrado
import com.userManagement.businessLayer.dto.UserDTOs;
import com.userManagement.businessLayer.service.AuthService;
import com.userManagement.businessLayer.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // 1. ENDPOINT DE REGISTRO POR PRIMERA VEZ (Público)
    @PostMapping("/register")
    @Operation(summary = "Registro de nuevo usuario", description = "Crea una cuenta en estado INACTIVE y envía un correo de verificación")
    public ResponseEntity<UserDTOs.UserResponse> register(@Valid @RequestBody UserDTOs.CreateUserRequest request) {
        UserDTOs.UserResponse createdUser = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    // 2. ENDPOINT DE VERIFICACIÓN DE CORREO (Público)
    @GetMapping("/verify")
    @Operation(summary = "Verificar cuenta por correo", description = "Activa la cuenta del usuario mediante el token enviado a su email")
    public ResponseEntity<String> verifyAccount(@RequestParam("token") String token) {
        String message = userService.verifyAccount(token);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/check-email")
    @Operation(summary = "Verificar existencia de correo", description = "Valida si un correo electrónico ya se encuentra registrado en la base de datos")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam("email") String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }
}