package petcare.petcare.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import petcare.petcare.model.*;
import petcare.petcare.repository.*;

import java.util.Arrays;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final MascotaRepository mascotaRepository;
    private final CitaRepository citaRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final DuenoRepository duenoRepository;

    @Value("${APP_ADMIN_EMAIL}")
    private String adminEmail;

    @GetMapping("/admin")
    public String panelAdmin(@AuthenticationPrincipal OAuth2User principal,
                             @RequestParam(required = false) String especie,
                             @RequestParam(defaultValue = "mascotas") String tab,
                             Model model, @PageableDefault(size = 10) Pageable pageable) {
        if (principal != null) {
            String email = principal.getAttribute("email");
            if (adminEmail.equals(email)) {
                User user = userRepository.findByEmail(email).orElse(null);
                model.addAttribute("usuario", user);
                model.addAttribute("numUsuarios", userRepository.count());
                model.addAttribute("numMascotas", mascotaRepository.count());
                model.addAttribute("numCitas", citaRepository.count());
                model.addAttribute("numVeterinarios", veterinarioRepository.count());

                // Paginación para todas las entidades
                Page<Mascota> mascotas;
                if (especie != null && !especie.isEmpty()) {
                    if ("OTRO".equals(especie)) {
                        mascotas = mascotaRepository.findByEspecieNotIn(Arrays.asList("PERRO", "GATO"), pageable);
                    } else {
                        mascotas = mascotaRepository.findByEspecieIgnoreCase(especie, pageable);
                    }
                    model.addAttribute("especieFiltro", especie);
                } else {
                    mascotas = mascotaRepository.findAll(pageable);
                }
                model.addAttribute("mascotas", mascotas);

                Page<Veterinario> veterinarios = veterinarioRepository.findAll(pageable);
                Page<User> usuarios = userRepository.findAll(pageable);
                Page<Dueno> duenos = duenoRepository.findAll(pageable);
                Page<Cita> citas = citaRepository.findAll(pageable);

                model.addAttribute("veterinarios", veterinarios);
                model.addAttribute("usuarios", usuarios);
                model.addAttribute("duenos", duenos);
                model.addAttribute("citas", citas);
                model.addAttribute("adminEmail", adminEmail);
                model.addAttribute("especies", List.of("PERRO", "GATO", "OTRO"));
                model.addAttribute("currentTab", tab);


                return "admin";
            }
        }
        return "redirect:/dashboard";
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
