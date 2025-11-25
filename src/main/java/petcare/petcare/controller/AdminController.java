package petcare.petcare.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import petcare.petcare.model.Mascota;
import petcare.petcare.model.User;
import petcare.petcare.repository.CitaRepository;
import petcare.petcare.repository.MascotaRepository;
import petcare.petcare.repository.VeterinarioRepository;
import petcare.petcare.repository.UserRepository;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final MascotaRepository mascotaRepository;
    private final CitaRepository citaRepository;
    private final VeterinarioRepository veterinarioRepository;

    @Value("${APP_ADMIN_EMAIL}")
    private String adminEmail;

    @GetMapping("/admin")
    public String panelAdmin(@AuthenticationPrincipal OAuth2User principal,
            @RequestParam(required = false) String especie,
            Model model) {
        if (principal != null) {
            String email = principal.getAttribute("email");
            if (adminEmail.equals(email)) {
                User user = userRepository.findByEmail(email).orElse(null);
                model.addAttribute("usuario", user);
                model.addAttribute("numUsuarios", userRepository.count());
                model.addAttribute("numMascotas", mascotaRepository.count());
                model.addAttribute("numCitas", citaRepository.count());
                model.addAttribute("numVeterinarios", veterinarioRepository.count());

                // Filtrado por especie
                List<Mascota> mascotas;
                if (especie != null && !especie.isEmpty()) {
                    if ("OTRO".equals(especie)) {
                        // Obtener todos excepto PERRO y GATO
                        mascotas = mascotaRepository.findAll().stream()
                                .filter(m -> !m.getEspecie().equalsIgnoreCase("PERRO") &&
                                        !m.getEspecie().equalsIgnoreCase("GATO"))
                                .toList();
                    } else {
                        mascotas = mascotaRepository.findByEspecieIgnoreCase(especie);
                    }
                    model.addAttribute("especieFiltro", especie);
                } else {
                    mascotas = mascotaRepository.findAll();
                }
                model.addAttribute("mascotas", mascotas);
                model.addAttribute("adminEmail", adminEmail);

                // Lista de especies disponibles
                model.addAttribute("especies", List.of("PERRO", "GATO", "OTRO"));

                return "admin";
            }
        }
        return "redirect:/dashboard"; // or "access-denied"
    }

    @GetMapping("/admin/mascota/{id}")
    public String verDetalleMascota(@AuthenticationPrincipal OAuth2User principal,
            @PathVariable Long id,
            Model model) {
        if (principal != null) {
            String email = principal.getAttribute("email");
            if (adminEmail.equals(email)) {
                Mascota mascota = mascotaRepository.findById(id).orElse(null);
                if (mascota != null) {
                    User user = userRepository.findByEmail(email).orElse(null);
                    model.addAttribute("usuario", user);
                    model.addAttribute("mascota", mascota);
                    model.addAttribute("adminEmail", adminEmail);
                    return "mascota-detalle";
                }
            }
        }
        return "redirect:/admin";
    }
}
