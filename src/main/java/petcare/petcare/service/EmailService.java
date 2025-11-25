package petcare.petcare.service;

import io.mailtrap.client.MailtrapClient;
import io.mailtrap.config.MailtrapConfig;
import io.mailtrap.factory.MailtrapClientFactory;
import io.mailtrap.model.request.emails.Address;
import io.mailtrap.model.request.emails.MailtrapMail;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${mailtrap.token}")
    private String token;

    @Value("${mailtrap.sender:no-reply@demomailtrap.co}")
    private String senderEmail;

    /**
     * Crea el cliente Mailtrap usando tu token
     */
    private MailtrapClient createClient() {
        MailtrapConfig config = new MailtrapConfig.Builder()
                .token(token)
                .build();
        return MailtrapClientFactory.createMailtrapClient(config);
    }

    /**
     * ENVÍO DEL EMAIL DE BIENVENIDA
     */
    @Async
    public void sendWelcomeEmail(String to, String userName) {
        MailtrapClient client = createClient();

        String subject = "¡Bienvenido a PetCare!";
        String text = "Hola " + userName + ",\n\n" +
                "¡Bienvenido a PetCare! 🐾\n" +
                "Gracias por registrarte usando Google/GitHub.\n\n" +
                "Ya puedes gestionar tus mascotas y acceder al dashboard.\n\n" +
                "Un saludo,\nEl equipo de PetCare";

        MailtrapMail mail = MailtrapMail.builder()
                .from(new Address(senderEmail))
                .to(List.of(new Address(to)))
                .subject(subject)
                .text(text)
                .build();

        try {
            client.send(mail);
            System.out.println("📨 Enviado email de bienvenida a: " + to);
        } catch (Exception e) {
            System.err.println("❌ Error enviando correo: " + e.getMessage());
        }
    }
}

