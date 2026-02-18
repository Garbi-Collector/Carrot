package gabri.dev.chatapp.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Servicio para envío de emails.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.front.url}")
    private String appUrl;

    /**
     * Envía email de verificación de forma asíncrona.
     */
    @Async
    public void sendVerificationEmail(String toEmail, String username, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Verifica tu cuenta en Carrot");

            String verificationUrl = appUrl + "auth/verify?token=" + token;

            String emailBody = String.format(
                    "Hola %s,\n\n" +
                            "Gracias por registrarte en Carrot!\n\n" +
                            "Por favor verifica tu dirección de email haciendo clic en el siguiente enlace:\n\n" +
                            "%s\n\n" +
                            "Si no creaste esta cuenta, puedes ignorar este email.\n\n" +
                            "Saludos,\n" +
                            "El equipo de Carrot",
                    username, verificationUrl
            );

            message.setText(emailBody);

            mailSender.send(message);
            log.info("Email de verificación enviado a: {}", toEmail);

        } catch (Exception e) {
            log.error("Error al enviar email de verificación a {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Envía email de confirmación de verificación exitosa.
     */
    @Async
    public void sendWelcomeEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("¡Bienvenido a Carrot!");

            String emailBody = String.format(
                    "Hola %s,\n\n" +
                            "Tu cuenta ha sido verificada exitosamente.\n\n" +
                            "Ya puedes iniciar sesión y comenzar a chatear.\n\n" +
                            "¡Que disfrutes de Carrot!\n\n" +
                            "Saludos,\n" +
                            "El equipo de Carrot",
                    username
            );

            message.setText(emailBody);

            mailSender.send(message);
            log.info("Email de bienvenida enviado a: {}", toEmail);

        } catch (Exception e) {
            log.error("Error al enviar email de bienvenida a {}: {}", toEmail, e.getMessage());
        }
    }
}