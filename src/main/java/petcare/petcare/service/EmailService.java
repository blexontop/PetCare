package petcare.petcare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void enviarCorreo(String para, String asunto, String texto) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(para);
        mensaje.setSubject(asunto);
        mensaje.setText(texto);
        mailSender.send(mensaje);
    }

    public void enviarBienvenida(String emailDestino, String nombre) {
        enviarCorreo(emailDestino,
                "Bienvenido a Petcare",
                "Hola " + nombre + ",\n\nGracias por registrarte en Petcare.\n\nEl equipo de Petcare");
    }
}
