package petcare.petcare.security;

import lombok.RequiredArgsConstructor;  
import org.springframework.security.core.userdetails.UserDetails; 
import org.springframework.security.core.userdetails.UserDetailsService; 
import org.springframework.security.core.userdetails.UsernameNotFoundException; 
import org.springframework.stereotype.Service; 
import petcare.petcare.model.User; 
import petcare.petcare.repository.UserRepository; 

import java.util.Collections;  

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
