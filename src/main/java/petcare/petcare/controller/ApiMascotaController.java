package petcare.petcare.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import petcare.petcare.model.Mascota;
import petcare.petcare.repository.MascotaRepository;

import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
@RequiredArgsConstructor
public class ApiMascotaController {

    private final MascotaRepository mascotaRepository;

    @GetMapping
    public List<Mascota> listarMascotas() {
        return mascotaRepository.findAll();
    }
}
