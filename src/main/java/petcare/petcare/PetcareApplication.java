package petcare.petcare;   // 👈 OJO: este package, NO ".service"

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.ApplicationRunner;
import petcare.petcare.repository.UserRepository;


@SpringBootApplication
@EnableScheduling   // para tareas programadas (resúmenes)
@EnableAsync        // para enviar correos con @Async
public class PetcareApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetcareApplication.class, args);
    }

    // Para consumir APIs externas (Dog/Cat, tiempo, países, etc.)
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // Ensure the known admin user has the ADMIN role on startup (non-destructive)
    @Bean
    public ApplicationRunner ensureAdmin(UserRepository userRepository) {
        return args -> {
            String adminEmail = "misaelbarreraojedagit@gmail.com";
            userRepository.findByEmail(adminEmail).ifPresent(u -> {
                if (u.getRoles() == null || !u.getRoles().contains("ROLE_ADMIN")) {
                    u.addRole("ROLE_ADMIN");
                    userRepository.save(u);
                }
            });
        };
    }
}
