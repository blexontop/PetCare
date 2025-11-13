package petcare.petcare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import petcare.petcare.model.Cita;
import petcare.petcare.model.Mascota;
import petcare.petcare.model.Veterinario;
import petcare.petcare.model.EstadoCita;

import java.time.LocalDateTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByMascota(Mascota mascota);

    List<Cita> findByVeterinario(Veterinario veterinario);

    List<Cita> findByEstado(EstadoCita estado);

    List<Cita> findByFechaHoraBetween(LocalDateTime desde, LocalDateTime hasta);
}
