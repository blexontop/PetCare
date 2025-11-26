package petcare.petcare.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import petcare.petcare.model.Veterinario;
import petcare.petcare.repository.VeterinarioRepository;

import java.util.List;

@RestController
@RequestMapping("/api/veterinarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ApiVeterinarios {

    private final VeterinarioRepository repo;

    @GetMapping
    public List<Veterinario> getVeterinarios() {
        return repo.findAll();
    }
}
