package petcare.petcare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:your-email@example.com}")
    private String from;

    @Value("${app.frontend.base-url:http://localhost:8080}")
    private String frontendBaseUrl;

    // 1) Email de bienvenida al registrarse un nuevo usuario
    @Async
    public void sendWelcomeEmail(String to, String userName) {
        String subject = "¡Bienvenido a Petcare!";
        String text = "Hola " + userName + ",\n\n" +
                "Gracias por registrarte en Petcare usando tu cuenta OAuth.\n\n" +
                "Un saludo,\nEl equipo de Petcare";

        sendSimpleMail(to, subject, text);
    }

    // 2) Confirmación de cuenta nuevo usuario
    @Async
    public void sendConfirmationEmail(String to, String confirmationToken) {
        String confirmUrl = frontendBaseUrl + "/confirm-account?token=" + confirmationToken;

        String subject = "Confirma tu cuenta en Petcare";
        String text = "Gracias por registrarte.\n\n" +
                "Haz clic en el siguiente enlace para confirmar tu cuenta:\n" +
                confirmUrl + "\n\n" +
                "Si no fuiste tú, ignora este correo.";

        sendSimpleMail(to, subject, text);
    }

    // 3) Resetear contraseña (email con enlace)
    @Async
    public void sendPasswordResetEmail(String to, String resetToken) {
        String resetUrl = frontendBaseUrl + "/reset-password?token=" + resetToken;

        String subject = "Restablecer contraseña";
        String text = "Hemos recibido una solicitud para restablecer tu contraseña.\n\n" +
                "Entra en este enlace para cambiarla:\n" +
                resetUrl + "\n\n" +
                "Si no fuiste tú, ignora este correo.";

        sendSimpleMail(to, subject, text);
    }

    // 4) Resumen diario/semanal de actividad
    @Async
    public void sendActivitySummaryEmail(String to, String userName, String frequency, String summaryText) {
        String subject = "Resumen " + frequency + " de tu actividad en Petcare";
        String text = "Hola " + userName + ",\n\n" +
                "Aquí tienes tu resumen " + frequency + ":\n\n" +
                summaryText + "\n\n" +
                "¡Gracias por usar Petcare!";

        sendSimpleMail(to, subject, text);
    }

    // Método interno común
    private void sendSimpleMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
