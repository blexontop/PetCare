package petcare.petcare.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import petcare.petcare.model.AuthProvider;
import petcare.petcare.model.Dueno;
import petcare.petcare.model.User;
import petcare.petcare.repository.DuenoRepository;
import petcare.petcare.repository.UserRepository;
import petcare.petcare.service.EmailService;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final UserRepository userRepository;
    private final DuenoRepository duenoRepository;
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
                    Role userRole = email.equals("misaelbarreraojedagit@gmail.com") ? Role.ADMIN : Role.USER;
                    user = User.builder()
                            .email(email)
                            .name(name != null ? name : email)
                            .picture(picture)
                            .provider(AuthProvider.GOOGLE)
                            .role(userRole)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    user = userRepository.save(user);

                    // Crear también un registro en Dueno básico
                    Dueno dueno = Dueno.builder()
                            .nombre(name != null ? name : email)
                            .email(email)
                            .build();
                    duenoRepository.save(dueno);

                    // 💌 Email de bienvenida SOLO la primera vez
                    emailService.sendWelcomeEmail(user.getEmail(), user.getName());

                    // Redirigir al formulario de completar perfil
                    model.addAttribute("dueno", dueno);
                    return "dueno-form";
                } else {
                    // Usuario existe, verificar si el perfil de dueno está completo
                    Dueno dueno = duenoRepository.findByEmail(email).orElse(null);
                    if (dueno != null && (dueno.getTelefono() == null || dueno.getTelefono().isEmpty() ||
                        dueno.getDireccion() == null || dueno.getDireccion().isEmpty() ||
                        dueno.getCiudad() == null || dueno.getCiudad().isEmpty())) {
                        // Perfil incompleto, redirigir al formulario
                        model.addAttribute("dueno", dueno);
                        return "dueno-form";
                    }
                }
            }
        }
        model.addAttribute("usuario", user);
        return "dashboard";
    }

    @PostMapping("/dueno/completar")
    public String completarPerfilDueno(@ModelAttribute Dueno duenoForm,
                                       @AuthenticationPrincipal OAuth2User principal,
                                       RedirectAttributes redirectAttributes) {
        if (principal != null) {
            String email = principal.getAttribute("email");
            if (email != null) {
                Dueno dueno = duenoRepository.findByEmail(email).orElse(null);
                if (dueno != null) {
                    dueno.setTelefono(duenoForm.getTelefono());
                    dueno.setDireccion(duenoForm.getDireccion());
                    dueno.setCiudad(duenoForm.getCiudad());
                    duenoRepository.save(dueno);

                    redirectAttributes.addFlashAttribute("success", "Perfil completado exitosamente");
                    return "redirect:/dashboard";
                }
            }
        }
        redirectAttributes.addFlashAttribute("error", "Error al completar el perfil");
        return "redirect:/dashboard";
    }


}
