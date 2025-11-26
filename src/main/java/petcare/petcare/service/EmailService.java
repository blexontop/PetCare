package petcare.petcare.service;

import lombok.RequiredArgsConstructor; 
import org.springframework.beans.factory.annotation.Value; 
import org.springframework.mail.SimpleMailMessage; 
import org.springframework.mail.javamail.JavaMailSender; 
import org.springframework.scheduling.annotation.Async; 
import org.springframework.stereotype.Service; 

@Service // Anotamos esta clase como un servicio de Spring
@RequiredArgsConstructor // Lombok genera automáticamente el constructor con las dependencias 'final'
public class EmailService {

    // Inyectamos el mailSender que será responsable de enviar los correos
    private final JavaMailSender mailSender;

    // Leemos el correo desde el archivo application.properties
    @Value("${app.mail.from:your-email@example.com}")
    private String from;

    // Leemos la URL base del frontend desde el archivo application.properties
    @Value("${app.frontend.base-url:http://localhost:8080}")
    private String frontendBaseUrl;

    // 1) Método para enviar un email de bienvenida al registrarse un nuevo usuario
    @Async // Este método se ejecuta de manera asíncrona para no bloquear el hilo principal
    public void sendWelcomeEmail(String to, String userName) {
        String subject = "¡Bienvenido a Petcare!"; // Asunto del email
        String text = "Hola " + userName + ",\n\n" + // Cuerpo del email
                "Gracias por registrarte en Petcare usando tu cuenta OAuth.\n\n" +
                "Un saludo,\nEl equipo de Petcare";

        sendSimpleMail(to, subject, text); // Enviamos el email utilizando el método interno
    }

    // 2) Método para enviar un email de confirmación de cuenta a un nuevo usuario
    @Async // Ejecutamos el método de forma asíncrona
    public void sendConfirmationEmail(String to, String confirmationToken) {
        String confirmUrl = frontendBaseUrl + "/confirm-account?token=" + confirmationToken; // Enlace de confirmación

        String subject = "Confirma tu cuenta en Petcare"; // Asunto del email
        String text = "Gracias por registrarte.\n\n" +
                "Haz clic en el siguiente enlace para confirmar tu cuenta:\n" +
                confirmUrl + "\n\n" +
                "Si no fuiste tú, ignora este correo."; // Cuerpo del email

        sendSimpleMail(to, subject, text); // Enviamos el email
    }

    // 3) Método para enviar un email de restablecimiento de contraseña
    @Async // Ejecutamos el método de forma asíncrona
    public void sendPasswordResetEmail(String to, String resetToken) {
        String resetUrl = frontendBaseUrl + "/reset-password?token=" + resetToken; // Enlace para restablecer la contraseña

        String subject = "Restablecer contraseña"; // Asunto del email
        String text = "Hemos recibido una solicitud para restablecer tu contraseña.\n\n" +
                "Entra en este enlace para cambiarla:\n" +
                resetUrl + "\n\n" +
                "Si no fuiste tú, ignora este correo."; // Cuerpo del email

        sendSimpleMail(to, subject, text); // Enviamos el email
    }

    // 4) Método para enviar un resumen de actividad (diario/semanal) a un usuario
    @Async // Ejecutamos el método de forma asíncrona
    public void sendActivitySummaryEmail(String to, String userName, String frequency, String summaryText) {
        String subject = "Resumen " + frequency + " de tu actividad en Petcare"; // Asunto del email
        String text = "Hola " + userName + ",\n\n" +
                "Aquí tienes tu resumen " + frequency + ":\n\n" +
                summaryText + "\n\n" +
                "¡Gracias por usar Petcare!"; // Cuerpo del email

        sendSimpleMail(to, subject, text); // Enviamos el email
    }

    // Método interno común para enviar emails
    private void sendSimpleMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage(); // Creamos un nuevo objeto de mensaje de correo
        message.setFrom(from); // Establecemos la dirección de correo de origen
        message.setTo(to); // Establecemos la dirección de correo de destino
        message.setSubject(subject); // Establecemos el asunto del correo
        message.setText(text); // Establecemos el contenido del correo

        mailSender.send(message); // Enviamos el correo utilizando el mailSender
    }
}
