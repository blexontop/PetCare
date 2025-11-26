package petcare.petcare;   // este package, NO ".service"

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

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
}
