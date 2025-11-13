package petcare.petcare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import petcare.petcare.model.Mascota;
import petcare.petcare.model.Dueno;

import java.util.List;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    List<Mascota> findByDueno(Dueno dueno);

    List<Mascota> findByEspecieIgnoreCase(String especie);
}
