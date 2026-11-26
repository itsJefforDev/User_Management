package com.userManagement.businessLayer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /** Activación por enlace (cuentas creadas por el administrador). */
    public void sendVerificationEmail(String toEmail, String token) {
        String subject = "Confirma tu cuenta - Finanzas Personales";
        // Enlace que apuntará al endpoint de tu backend para verificar la cuenta
        String verificationUrl = "http://localhost:8080/userManagement/api/v1/auth/verify?token=" + token;

        String message = "¡Hola!\n\nGracias por registrarte. Para activar tu cuenta y comprobar que este correo es tuyo, haz clic en el siguiente enlace:\n\n"
                + verificationUrl + "\n\nSi no solicitaste esta cuenta, puedes ignorar este mensaje.";

        send(toEmail, subject, message);
    }

    /** Código de 4 dígitos para activar una cuenta auto-registrada. */
    public void sendVerificationCodeEmail(String toEmail, String code, int minutes) {
        String message = "¡Hola!\n\nTu código de verificación es: " + code
                + "\n\nIngrésalo en la aplicación para activar tu cuenta. Vence en " + minutes + " minutos."
                + "\n\nSi no solicitaste esta cuenta, puedes ignorar este mensaje.";

        send(toEmail, "Tu código de verificación - Finanzas Personales", message);
    }

    /** Contraseña temporal del recordatorio de contraseña. */
    public void sendTemporaryPasswordEmail(String toEmail, String temporaryPassword, int minutes) {
        String message = "¡Hola!\n\nRecibimos una solicitud para recuperar tu contraseña."
                + "\n\nTu contraseña temporal es: " + temporaryPassword
                + "\n\nVence en " + minutes + " minutos. Al ingresar con ella se te pedirá crear una nueva contraseña."
                + "\n\nSi no fuiste tú, comunícate con el administrador.";

        send(toEmail, "Recordatorio de contraseña - Finanzas Personales", message);
    }

    private void send(String toEmail, String subject, String text) {
        SimpleMailMessage emailMessage = new SimpleMailMessage();
        emailMessage.setTo(toEmail);
        emailMessage.setSubject(subject);
        emailMessage.setText(text);
        emailMessage.setFrom("tu-correo@gmail.com");

        mailSender.send(emailMessage);
    }
}