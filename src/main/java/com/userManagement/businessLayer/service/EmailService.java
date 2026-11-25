package com.userManagement.businessLayer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String token) {
        String subject = "Confirma tu cuenta - Finanzas Personales";
        // Enlace que apuntará al endpoint de tu backend para verificar la cuenta
        String verificationUrl = "http://localhost:8080/userManagement/api/v1/auth/verify?token=" + token;

        String message = "¡Hola!\n\nGracias por registrarte. Para activar tu cuenta y comprobar que este correo es tuyo, haz clic en el siguiente enlace:\n\n"
                + verificationUrl + "\n\nSi no solicitaste esta cuenta, puedes ignorar este mensaje.";

        SimpleMailMessage emailMessage = new SimpleMailMessage();
        emailMessage.setTo(toEmail);
        emailMessage.setSubject(subject);
        emailMessage.setText(message);
        emailMessage.setFrom("tu-correo@gmail.com");

        mailSender.send(emailMessage);
    }
}