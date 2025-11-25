package petcare.petcare.service;

import io.mailtrap.client.MailtrapClient;
import io.mailtrap.config.MailtrapConfig;
import io.mailtrap.factory.MailtrapClientFactory;
import io.mailtrap.model.request.emails.Address;
import io.mailtrap.model.request.emails.MailtrapMail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MailtrapService {

    @Value("${mailtrap.token}")
    private String token;

    @Value("${mailtrap.sender}")
    private String senderEmail;

    private MailtrapClient getClient() {
        MailtrapConfig config = new MailtrapConfig.Builder()
                .token(token)
                .build();

        return MailtrapClientFactory.createMailtrapClient(config);
    }

    public void sendWelcomeEmail(String recipientEmail, String nombreUsuario) {
        MailtrapClient client = getClient();

        MailtrapMail mail = MailtrapMail.builder()
                .from(new Address(senderEmail))
                .to(List.of(new Address(recipientEmail)))
                .subject("¡Bienvenido a PetCare!")
                .text("Hola " + nombreUsuario + ",\n\n" +
                        "Gracias por registrarte en PetCare.\n" +
                        "Tu cuenta ha sido creada correctamente.\n\n" +
                        "Un saludo,\nEquipo PetCare")
                .build();

        try {
            client.send(mail);
            System.out.println("Email enviado correctamente a " + recipientEmail);
        } catch (Exception e) {
            System.out.println("ERROR enviando email: " + e.getMessage());
        }
    }
}
