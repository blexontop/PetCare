package petcare.petcare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import petcare.petcare.model.Dueno;

import java.util.Optional;

public interface DuenoRepository extends JpaRepository<Dueno, Long> {
    Optional<Dueno> findByEmail(String email);
}
