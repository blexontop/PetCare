package petcare.petcare.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import petcare.petcare.service.EmailService;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EmailApiController {

    private final EmailService emailService;

    // Ej: GET /api/email/test-welcome?to=correo@ejemplo.com&name=Alejandro
    @GetMapping("/test-welcome")
    public ResponseEntity<String> testWelcome(@RequestParam String to,
                                              @RequestParam String name) {
        emailService.sendWelcomeEmail(to, name);
        return ResponseEntity.ok("Email de bienvenida enviado");
    }

    // Ej: GET /api/email/test-confirm?to=correo@ejemplo.com&token=abc123
    @GetMapping("/test-confirm")
    public ResponseEntity<String> testConfirm(@RequestParam String to,
                                              @RequestParam String token) {
        emailService.sendConfirmationEmail(to, token);
        return ResponseEntity.ok("Email de confirmación enviado");
    }

    // Ej: GET /api/email/test-reset?to=correo@ejemplo.com&token=xyz789
    @GetMapping("/test-reset")
    public ResponseEntity<String> testReset(@RequestParam String to,
                                            @RequestParam String token) {
        emailService.sendPasswordResetEmail(to, token);
        return ResponseEntity.ok("Email de reset enviado");
    }

    // Ej:
    // GET /api/email/test-summary?to=correo@ejemplo.com&name=Alejandro&frequency=diario&text=Resumen+de+prueba
    @GetMapping("/test-summary")
    public ResponseEntity<String> testSummary(@RequestParam String to,
                                              @RequestParam String name,
                                              @RequestParam String frequency,
                                              @RequestParam String text) {
        emailService.sendActivitySummaryEmail(to, name, frequency, text);
        return ResponseEntity.ok("Email de resumen enviado");
    }
}
