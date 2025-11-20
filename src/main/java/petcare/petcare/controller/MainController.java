package petcare.petcare.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import petcare.petcare.model.User;
import petcare.petcare.repository.UserRepository;
import petcare.petcare.service.EmailService;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final UserRepository userRepository;
    private final EmailService emailService;   // 👈 inyectamos EmailService

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal OAuth2User principal, Model model) {
        User user = null;
        if (principal != null) {
            String email = principal.getAttribute("email");
            String name = principal.getAttribute("name");
            String picture = principal.getAttribute("picture");

            if (email != null) {
                // Buscamos el usuario en la BBDD
                user = userRepository.findByEmail(email).orElse(null);

                // Si no existe → primer login = "registro"
                if (user == null) {
                    user = User.builder()
                            .email(email)
                            .name(name != null ? name : email)
                            .picture(picture)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    user = userRepository.save(user);

                    // 💌 Email de bienvenida SOLO la primera vez
                    emailService.sendWelcomeEmail(user.getEmail(), user.getName());
                }
            }
        }
        model.addAttribute("usuario", user);
        return "dashboard";
    }
}
