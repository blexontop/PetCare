package petcare.petcare.controller;

import lombok.RequiredArgsConstructor; 
import org.springframework.http.ResponseEntity; 
import org.springframework.web.bind.annotation.*; 
import petcare.petcare.service.EmailService;

@RestController // Esta clase maneja las solicitudes HTTP en una API REST
@RequestMapping("/api/email") // Define la ruta base para todas las peticiones dentro de este controlador
@CrossOrigin(origins = "*") // Permite peticiones de cualquier origen (para facilitar el acceso desde diferentes dominios)
@RequiredArgsConstructor // Lombok genera automáticamente el constructor con todas las dependencias 'final'
public class EmailApiController {

    // Inyectamos el servicio de correo electrónico para poder utilizarlo
    private final EmailService emailService;

    // Método para probar el envío de un email de bienvenida
    @GetMapping("/test-welcome")
    public ResponseEntity<String> testWelcome(@RequestParam String to, // Parámetro 'to' para la dirección de correo electrónico
                                              @RequestParam String name) { // Parámetro 'name' para el nombre del usuario
        emailService.sendWelcomeEmail(to, name); // Llamamos al servicio para enviar el email de bienvenida
        return ResponseEntity.ok("Email de bienvenida enviado"); // Respondemos con un mensaje de éxito
    }

    // Método para probar el envío de un email de confirmación
    @GetMapping("/test-confirm")
    public ResponseEntity<String> testConfirm(@RequestParam String to, // Parámetro 'to' para la dirección de correo electrónico
                                              @RequestParam String token) { // Parámetro 'token' para el token de confirmación
        emailService.sendConfirmationEmail(to, token); // Llamamos al servicio para enviar el email de confirmación
        return ResponseEntity.ok("Email de confirmación enviado"); // Respondemos con un mensaje de éxito
    }

    // Método para probar el envío de un email para restablecer la contraseña
    @GetMapping("/test-reset")
    public ResponseEntity<String> testReset(@RequestParam String to, // Parámetro 'to' para la dirección de correo electrónico
                                            @RequestParam String token) { // Parámetro 'token' para el token de restablecimiento de contraseña
        emailService.sendPasswordResetEmail(to, token); // Llamamos al servicio para enviar el email de restablecimiento de contraseña
        return ResponseEntity.ok("Email de reset enviado"); // Respondemos con un mensaje de éxito
    }

    // Método para probar el envío de un resumen de actividad al usuario
    @GetMapping("/test-summary")
    public ResponseEntity<String> testSummary(@RequestParam String to, // Parámetro 'to' para la dirección de correo electrónico
                                              @RequestParam String name, // Parámetro 'name' para el nombre del usuario
                                              @RequestParam String frequency, // Parámetro 'frequency' para la frecuencia del resumen (diario, semanal, etc.)
                                              @RequestParam String text) { // Parámetro 'text' para el contenido del resumen
        emailService.sendActivitySummaryEmail(to, name, frequency, text); // Llamamos al servicio para enviar el email con el resumen de actividad
        return ResponseEntity.ok("Email de resumen enviado"); // Respondemos con un mensaje de éxito
    }
}
