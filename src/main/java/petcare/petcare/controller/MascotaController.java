package petcare.petcare.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import petcare.petcare.model.Dueno;
import petcare.petcare.model.Mascota;
import petcare.petcare.repository.DuenoRepository;
import petcare.petcare.repository.MascotaRepository;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MascotaController {

    private final MascotaRepository mascotaRepository;
    private final DuenoRepository duenoRepository;

    @GetMapping("/mascotas")
    public String listarMascotas(Model model) {
        List<Mascota> mascotas = mascotaRepository.findAll();
        model.addAttribute("mascotas", mascotas);
        return "mascotas";
    }

    @GetMapping("/mascotas/nueva")
    public String mostrarFormularioNuevaMascota(Model model) {
        model.addAttribute("duenos", duenoRepository.findAll());
        return "mascota-form";
    }

    @PostMapping("/mascotas")
    public String guardarMascota(@RequestParam String nombre,
                                 @RequestParam String especie,
                                 @RequestParam(required = false) String raza,
                                 @RequestParam(required = false) String fechaNacimiento,
                                 @RequestParam(required = false) Double peso,
                                 @RequestParam Long duenoId) {

        Dueno dueno = duenoRepository.findById(duenoId).orElse(null);

        Mascota mascota = Mascota.builder()
                .nombre(nombre)
                .especie(especie)
                .raza(raza)
                .peso(peso)
                .dueno(dueno)
                .build();

        if (fechaNacimiento != null && !fechaNacimiento.isBlank()) {
            mascota.setFechaNacimiento(LocalDate.parse(fechaNacimiento));
        }

        mascotaRepository.save(mascota);
        return "redirect:/mascotas";
    }

    @GetMapping("/mascotas/{id}/eliminar")
    public String eliminarMascota(@PathVariable Long id) {
        mascotaRepository.deleteById(id);
        return "redirect:/mascotas";
    }
}
