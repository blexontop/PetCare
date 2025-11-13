package petcare.petcare.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // PARA HACER QUE FUNCIONE HAY QUE QUITAR LOS COMENTARIOS Y COMENTAR LA LINEA DE ANYREQUEST.PERMITALL
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/admin/**").authenticated()
                // .anyRequest().authenticated()  // Comentado temporalmente para deshabilitar login
                .anyRequest().permitAll()  // Permitir todo temporalmente
            )
            // .oauth2Login(oauth -> oauth
            //     .loginPage("/login")
            //     .defaultSuccessUrl("/dashboard", true)
            // )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }
}
