package petcare.petcare.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
    public String panelAdmin(Model model) {
        model.addAttribute("numUsuarios", userRepository.count());
        model.addAttribute("numMascotas", mascotaRepository.count());
        model.addAttribute("numCitas", citaRepository.count());
        model.addAttribute("numVeterinarios", veterinarioRepository.count());
        return "admin";
    }
}
