package com.userManagement.businessLayer.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Error de autenticación/verificación con un código funcional que el front usa
 * para decidir el flujo (ej. PASSWORD_EXPIRED abre el cambio obligatorio de contraseña).
 */
@Getter
public class AuthException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public AuthException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }
}