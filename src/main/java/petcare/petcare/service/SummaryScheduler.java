package petcare.petcare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import petcare.petcare.model.User;
import petcare.petcare.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class SummaryScheduler {

    private final UserRepository userRepository;
    private final EmailService emailService;

    // Enviar resumen DIARIO a las 20:00
    @Scheduled(cron = "0 0 20 * * *")
    public void sendDailySummaries() {
        for (User user : userRepository.findAll()) {
            String summaryText = "Este es tu resumen diario automático de Petcare (demo).";
            emailService.sendActivitySummaryEmail(
                    user.getEmail(),
                    user.getName(),
                    "diario",
                    summaryText
            );
        }
    }

    // Enviar resumen SEMANAL los lunes a las 20:00
    @Scheduled(cron = "0 0 20 * * MON")
    public void sendWeeklySummaries() {
        for (User user : userRepository.findAll()) {
            String summaryText = "Este es tu resumen semanal automático de Petcare (demo).";
            emailService.sendActivitySummaryEmail(
                    user.getEmail(),
                    user.getName(),
                    "semanal",
                    summaryText
            );
        }
    }
}
