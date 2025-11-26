package petcare.petcare.security;

import org.springframework.context.annotation.Bean; // Importa la anotación Bean para definir beans de Spring
import org.springframework.context.annotation.Configuration; // Importa la anotación Configuration para indicar que esta clase es de configuración
import org.springframework.security.authentication.AuthenticationManager; // Importa el AuthenticationManager para manejar la autenticación
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; // Configuración de autenticación de Spring Security
import org.springframework.security.config.annotation.web.builders.HttpSecurity; // Importa la clase HttpSecurity para configurar la seguridad HTTP
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // Activa la configuración de seguridad web en Spring
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Importa el codificador de contraseñas BCrypt
import org.springframework.security.crypto.password.PasswordEncoder; // Interfaz para codificar contraseñas
import org.springframework.security.web.SecurityFilterChain; // Importa la clase SecurityFilterChain para configurar filtros de seguridad
import lombok.RequiredArgsConstructor; // Importa la anotación RequiredArgsConstructor de Lombok para crear el constructor con los campos requeridos
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder; // Utilizado para construir el AuthenticationManager

@Configuration // Indica que esta clase contiene configuración de Spring
@EnableWebSecurity // Habilita la configuración de seguridad web
@RequiredArgsConstructor // Lombok generará un constructor con los campos finales que necesitan ser
                         // inyectados
public class SecurityConfig {

        private final CustomUserDetailsService customUserDetailsService; // Servicio personalizado para cargar los
                                                                         // detalles del usuario
        private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler; // Manejador para el éxito
                                                                                             // de la autenticación

        // Bean para crear el codificador de contraseñas BCrypt
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(); // Utiliza el algoritmo BCrypt para codificar contraseñas
        }

        // Bean para configurar los filtros de seguridad HTTP
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(authz -> authz
                                                // Rutas públicas (sin autenticación requerida)
                                                .requestMatchers("/", "/login", "/register", "/guest-login", "/css/**",
                                                                "/js/**",
                                                                "/images/**", "/api/**", "/mapa")
                                                .permitAll()
                                                // Rutas de admin requieren verificación
                                                .requestMatchers("/admin/**").permitAll()
                                                // Rutas de mascotas requieren autenticación
                                                .requestMatchers("/mascotas/**").authenticated()
                                                // El dashboard requiere autenticación
                                                .requestMatchers("/dashboard").authenticated()
                                                // Cualquier otra ruta es pública
                                                .anyRequest().permitAll())
                                .formLogin(form -> form
                                                .loginPage("/login") // Página de login personalizada
                                                .failureUrl("/login?error=true") // URL en caso de error en el login
                                                .successHandler(customAuthenticationSuccessHandler) // Manejador en caso
                                                                                                    // de éxito en el
                                                                                                    // login
                                                .permitAll()) // Permite acceso a la página de login sin autenticación
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/login") // Página de login para OAuth2
                                                .successHandler(customAuthenticationSuccessHandler)) // Manejador en
                                                                                                     // caso de éxito de
                                                                                                     // login OAuth2
                                .logout(logout -> logout
                                                .logoutSuccessUrl("/")); // URL de redirección después de hacer logout

                return http.build(); // Construye la configuración de seguridad
        }

        // Bean para configurar el AuthenticationManager
        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager(); // Obtiene el AuthenticationManager desde la configuración
                                                              // de autenticación
        }

        // Configura la autenticación usando un servicio personalizado y un codificador
        // de contraseñas
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
                auth.userDetailsService(customUserDetailsService) // Usamos el servicio personalizado para cargar los
                                                                  // detalles del usuario
                                .passwordEncoder(passwordEncoder()); // Usamos el codificador de contraseñas BCrypt
        }
}