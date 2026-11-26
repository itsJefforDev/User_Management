package com.userManagement.presentationLayer.controller;

import com.userManagement.businessLayer.dto.AuthDTOs.MessageResponse;
import com.userManagement.businessLayer.dto.UserDTOs.*;
import com.userManagement.businessLayer.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Contraseña temporal aleatoria de 8 dígitos para el formulario del administrador
    @GetMapping("/temporary-password")
    public ResponseEntity<TemporaryPasswordResponse> generateTemporaryPassword() {
        return ResponseEntity.ok(userService.generateTemporaryPassword());
    }

    // Envía el correo de activación por enlace a una cuenta inactiva
    @PostMapping("/{id}/send-activation")
    public ResponseEntity<MessageResponse> sendActivationEmail(@PathVariable Long id) {
        userService.sendActivationEmail(id);
        return ResponseEntity.ok(new MessageResponse("Correo de activación enviado correctamente."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}