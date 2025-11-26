package petcare.petcare.repository;

// Importa la interfaz JpaRepository para realizar operaciones con la base de datos
import org.springframework.data.jpa.repository.JpaRepository;

// Importa la clase Dueno, que es la entidad que representa a un dueño
import petcare.petcare.model.Dueno;

// Importa la clase Optional, que se usa para manejar valores que pueden estar presentes o no

import java.util.Optional;

// Interfaz DuenoRepository que extiende JpaRepository
// JpaRepository proporciona métodos para realizar operaciones sobre la entidad Dueno
// El primer parámetro es la entidad (Dueno) y el segundo es el tipo de dato de su identificador (Long)
public interface DuenoRepository extends JpaRepository<Dueno, Long> {

    // Metodo para encontrar un dueño por su email
    // Optional es utilizado para evitar errores en caso de que no se encuentre un
    // dueño con el email proporcionado
    Optional<Dueno> findByEmail(String email);
}
