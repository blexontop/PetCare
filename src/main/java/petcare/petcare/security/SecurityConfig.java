package petcare.petcare.security;

import org.springframework.context.annotation.Bean; 
import org.springframework.context.annotation.Configuration; 
import org.springframework.security.authentication.AuthenticationManager; 
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; 
import org.springframework.security.config.annotation.web.builders.HttpSecurity;  
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;  
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;  
import org.springframework.security.crypto.password.PasswordEncoder; 
import org.springframework.security.web.SecurityFilterChain; 
import lombok.RequiredArgsConstructor;  
import petcare.petcare.security.CustomUserDetailsService;  
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;  

@Configuration  // Indica que esta clase contiene configuración de Spring
@EnableWebSecurity  // Habilita la configuración de seguridad web
@RequiredArgsConstructor  // Lombok generará un constructor con los campos finales que necesitan ser inyectados
public class SecurityConfig {

        private final CustomUserDetailsService customUserDetailsService;  // Servicio personalizado para cargar los detalles del usuario
        private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;  // Manejador para el éxito de la autenticación

        // Bean para crear el codificador de contraseñas BCrypt
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();  // Utiliza el algoritmo BCrypt para codificar contraseñas
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
                                                .loginPage("/login")  // Página de login personalizada
                                                .failureUrl("/login?error=true")  // URL en caso de error en el login
                                                .successHandler(customAuthenticationSuccessHandler)  // Manejador en caso de éxito en el login
                                                .permitAll())  // Permite acceso a la página de login sin autenticación
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/login")  // Página de login para OAuth2
                                                .successHandler(customAuthenticationSuccessHandler))  // Manejador en caso de éxito de login OAuth2
                                .logout(logout -> logout
                                                .logoutSuccessUrl("/"));  // URL de redirección después de hacer logout

                return http.build();  // Construye la configuración de seguridad
        }

        // Bean para configurar el AuthenticationManager
        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();  // Obtiene el AuthenticationManager desde la configuración de autenticación
        }

        // Configura la autenticación usando un servicio personalizado y un codificador de contraseñas
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
                auth.userDetailsService(customUserDetailsService)  // Usamos el servicio personalizado para cargar los detalles del usuario
                                .passwordEncoder(passwordEncoder());  // Usamos el codificador de contraseñas BCrypt
        }
}