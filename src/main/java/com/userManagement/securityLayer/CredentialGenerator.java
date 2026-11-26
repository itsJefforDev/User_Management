package com.userManagement.securityLayer;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/** Genera credenciales aleatorias con SecureRandom. */
@Component
public class CredentialGenerator {

    private final SecureRandom random = new SecureRandom();

    /** Contraseña temporal de 8 dígitos (con ceros a la izquierda). */
    public String generateTemporaryPassword() {
        return String.format("%08d", random.nextInt(100_000_000));
    }

    /** Código de verificación de 4 dígitos (con ceros a la izquierda). */
    public String generateVerificationCode() {
        return String.format("%04d", random.nextInt(10_000));
    }
}