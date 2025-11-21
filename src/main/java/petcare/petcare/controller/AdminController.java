package petcare.petcare.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import petcare.petcare.model.User;
import petcare.petcare.repository.CitaRepository;
import petcare.petcare.repository.MascotaRepository;
import petcare.petcare.repository.VeterinarioRepository;
import petcare.petcare.repository.UserRepository;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final MascotaRepository mascotaRepository;
    private final CitaRepository citaRepository;
    private final VeterinarioRepository veterinarioRepository;

    @GetMapping("/admin")
    public String panelAdmin(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal != null) {
            String email = principal.getAttribute("email");
            if ("misaelbarreraojedagit@gmail.com".equals(email)) {
                User user = userRepository.findByEmail(email).orElse(null);
                model.addAttribute("usuario", user);
                model.addAttribute("numUsuarios", userRepository.count());
                model.addAttribute("numMascotas", mascotaRepository.count());
                model.addAttribute("numCitas", citaRepository.count());
                model.addAttribute("numVeterinarios", veterinarioRepository.count());
                model.addAttribute("mascotas", mascotaRepository.findAll());
                return "admin";
            }
        }
        return "redirect:/dashboard"; // or "access-denied"
    }
}
