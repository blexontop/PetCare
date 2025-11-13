package petcare.petcare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import petcare.petcare.model.Veterinario;

import java.util.List;

public interface VeterinarioRepository extends JpaRepository<Veterinario, Long> {

    List<Veterinario> findByCiudadIgnoreCase(String ciudad);
}
