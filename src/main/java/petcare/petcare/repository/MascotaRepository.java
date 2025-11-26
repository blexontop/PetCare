package petcare.petcare.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import petcare.petcare.model.Mascota;
import petcare.petcare.model.Dueno;

import java.util.List;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    // Encuentra las mascotas de un dueño específico
    List<Mascota> findByDueno(Dueno dueno);

    // Encuentra las mascotas por especie (sin importar mayúsculas/minúsculas)
    List<Mascota> findByEspecieIgnoreCase(String especie);

    // Encuentra las mascotas por especie con paginación
    Page<Mascota> findByEspecieIgnoreCase(String especie, Pageable pageable);

    // Encuentra todas las mascotas con paginación
    Page<Mascota> findAll(Pageable pageable);

    // Encuentra mascotas cuya especie no esté en la lista de especies proporcionada, con paginación
    Page<Mascota> findByEspecieNotIn(List<String> especies, Pageable pageable);
}
