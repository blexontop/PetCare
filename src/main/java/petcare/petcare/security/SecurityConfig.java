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
// import petcare.petcare.security.CustomUserDetailsService; // Comentado: clase del mismo paquete no necesita import
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final CustomUserDetailsService customUserDetailsService;
        private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(authz -> authz
                                                // Rutas públicas
                                                .requestMatchers("/", "/login", "/register", "/guest-login", "/css/**",
                                                                "/js/**",
                                                                "/images/**", "/api/**", "/mapa")
                                                .permitAll()
                                                // Admin requiere verificación en el controlador
                                                .requestMatchers("/admin/**").permitAll()
                                                // Mascotas requieren autenticación
                                                .requestMatchers("/mascotas/**").authenticated()
                                                // Dashboard requiere autenticación
                                                .requestMatchers("/dashboard").authenticated()
                                                // Todo lo demás es público
                                                .anyRequest().permitAll())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .failureUrl("/login?error=true")
                                                .successHandler(customAuthenticationSuccessHandler)
                                                .permitAll())
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/login")
                                                .successHandler(customAuthenticationSuccessHandler))
                                .logout(logout -> logout
                                                .logoutSuccessUrl("/"));

                return http.build();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }

        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
                auth.userDetailsService(customUserDetailsService)
                                .passwordEncoder(passwordEncoder());
        }
}
