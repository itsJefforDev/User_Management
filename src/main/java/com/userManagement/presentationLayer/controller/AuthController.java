package com.userManagement.presentationLayer.controller;

import com.userManagement.businessLayer.dto.AuthDTOs.*;
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
    @Operation(summary = "Registro de nuevo usuario", description = "Crea una cuenta en estado INACTIVE y envía un código de 4 dígitos al correo")
    public ResponseEntity<UserDTOs.UserResponse> register(@Valid @RequestBody UserDTOs.CreateUserRequest request) {
        UserDTOs.UserResponse createdUser = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    // 2. ACTIVACIÓN POR ENLACE (cuentas creadas por el administrador)
    @GetMapping("/verify")
    @Operation(summary = "Verificar cuenta por enlace", description = "Activa la cuenta del usuario mediante el token enviado a su email")
    public ResponseEntity<String> verifyAccount(@RequestParam("token") String token) {
        String message = userService.verifyAccount(token);
        return ResponseEntity.ok(message);
    }

    // 3. ACTIVACIÓN POR CÓDIGO DE 4 DÍGITOS (auto-registro)
    @PostMapping("/verify-code")
    @Operation(summary = "Verificar cuenta por código", description = "Activa la cuenta con el código de 4 dígitos enviado al correo")
    public ResponseEntity<MessageResponse> verifyCode(@RequestBody VerifyCodeRequest request) {
        String message = userService.verifyCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(new MessageResponse(message));
    }

    @PostMapping("/resend-code")
    @Operation(summary = "Reenviar código de verificación", description = "Genera y envía un nuevo código de 4 dígitos")
    public ResponseEntity<MessageResponse> resendCode(@RequestBody ResendCodeRequest request) {
        userService.resendVerificationCode(request.getEmail());
        return ResponseEntity.ok(new MessageResponse("Si la cuenta está pendiente de verificación, enviamos un nuevo código a tu correo."));
    }

    // 4. RECORDATORIO DE CONTRASEÑA: envía una contraseña temporal de 8 dígitos
    @PostMapping("/forgot-password")
    @Operation(summary = "Recordatorio de contraseña", description = "Envía al correo una contraseña temporal de 8 dígitos")
    public ResponseEntity<MessageResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(new MessageResponse("Si el correo está registrado, enviamos una contraseña temporal."));
    }

    // 5. CAMBIO DE CONTRASEÑA (obligatorio tras una contraseña temporal)
    @PostMapping("/change-password")
    @Operation(summary = "Cambiar contraseña", description = "Valida la contraseña actual (o temporal) y establece la nueva")
    public ResponseEntity<MessageResponse> changePassword(@RequestBody ChangePasswordRequest request) {
        authService.changePassword(request.getEmail(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(new MessageResponse("Contraseña actualizada correctamente."));
    }

    @GetMapping("/check-email")
    @Operation(summary = "Verificar existencia de correo", description = "Valida si un correo electrónico ya se encuentra registrado en la base de datos")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam("email") String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }
}