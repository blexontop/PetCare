package petcare.petcare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import petcare.petcare.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
