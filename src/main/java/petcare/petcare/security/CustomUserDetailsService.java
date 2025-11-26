package petcare.petcare.security;

import lombok.RequiredArgsConstructor;  // Anotación de Lombok para generar un constructor con los campos finales requeridos
import org.springframework.security.core.userdetails.UserDetails;  // Importa la interfaz UserDetails para proporcionar detalles de un usuario
import org.springframework.security.core.userdetails.UserDetailsService;  // Importa la interfaz UserDetailsService para cargar los detalles del usuario
import org.springframework.security.core.userdetails.UsernameNotFoundException;  // Excepción lanzada cuando no se encuentra un usuario
import org.springframework.stereotype.Service;  // Anotación que marca esta clase como un servicio de Spring
import petcare.petcare.model.User;  // Importa la clase User que representa a un usuario en la base de datos
import petcare.petcare.repository.UserRepository;  // Importa el repositorio de usuarios para interactuar con la base de datos

import java.util.Collections;  // Importa Collections para trabajar con colecciones inmutables

@Service  // Anotación que marca esta clase como un servicio gestionado por Spring
@RequiredArgsConstructor  // Lombok generará un constructor con los campos finales de la clase
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;  // Repositorio que maneja la persistencia de usuarios

    // Método que carga los detalles del usuario por su correo electrónico
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Busca el usuario por su correo electrónico en el repositorio
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Construye un objeto UserDetails con los detalles del usuario
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())  // Establece el correo electrónico como nombre de usuario
                .password(user.getPassword())  // Establece la contraseña del usuario
                .authorities(Collections.emptyList())  // No asigna roles ni privilegios, puede modificarse según se necesite
                .build();  // Devuelve el objeto UserDetails construido
    }
}
