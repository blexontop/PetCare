package petcare.petcare.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import petcare.petcare.model.Veterinario;
import petcare.petcare.repository.VeterinarioRepository;

import java.util.List;

@RestController                                 // Indica que esta clase es un controlador REST
@RequestMapping("/api/veterinarios")            // Ruta base para todos los endpoints de este controlador
@RequiredArgsConstructor                         // Lombok genera automáticamente el constructor con los "final"
@CrossOrigin(origins = "*")                     // Permite solicitudes desde cualquier dominio (CORS)
public class ApiVeterinarios {

    private final VeterinarioRepository repo;   // Repositorio inyectado automáticamente gracias a @RequiredArgsConstructor

    @GetMapping                                // GET /api/veterinarios
    public List<Veterinario> getVeterinarios() {
        // Retorna la lista completa de veterinarios desde la base de datos
        return repo.findAll();
    }
}
