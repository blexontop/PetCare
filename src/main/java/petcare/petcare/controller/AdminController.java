package petcare.petcare.controller;

import lombok.RequiredArgsConstructor; 
import org.springframework.beans.factory.annotation.Value; 
import org.springframework.data.domain.Page; 
import org.springframework.data.domain.Pageable; 
import org.springframework.data.web.PageableDefault; 
import org.springframework.security.core.annotation.AuthenticationPrincipal; 
import org.springframework.security.oauth2.core.user.OAuth2User; 
import org.springframework.stereotype.Controller; 
import org.springframework.ui.Model; 
import org.springframework.web.bind.annotation.GetMapping; 
import org.springframework.web.bind.annotation.PathVariable; 
import org.springframework.web.bind.annotation.RequestParam; 

import petcare.petcare.model.*; 
import petcare.petcare.repository.*; 

import java.util.Arrays; 
import java.util.List;

@Controller // Indicamos que esta clase es un controlador de Spring
@RequiredArgsConstructor // Lombok genera un constructor con todas las dependencias 'final'
public class AdminController {

    // Repositorios necesarios para interactuar con la base de datos
    private final UserRepository userRepository;
    private final MascotaRepository mascotaRepository;
    private final CitaRepository citaRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final DuenoRepository duenoRepository;

    // Leemos el correo del administrador desde el archivo de propiedades
    @Value("${APP_ADMIN_EMAIL}")
    private String adminEmail;

    // Método que maneja la página del panel de administración
    @GetMapping("/admin")
    public String panelAdmin(@AuthenticationPrincipal OAuth2User principal, // Obtiene el usuario autenticado
                             @RequestParam(required = false) String especie, // Parámetro para filtrar mascotas por especie
                             @RequestParam(defaultValue = "mascotas") String tab, // Parámetro para determinar qué pestaña se muestra en la interfaz
                             Model model, @PageableDefault(size = 10) Pageable pageable) { // Paginación de los resultados

        // Verificamos que el usuario esté autenticado
        if (principal != null) {
            String email = principal.getAttribute("email"); // Obtenemos el correo del usuario autenticado

            // Verificamos si el usuario autenticado es el administrador
            if (adminEmail.equals(email)) {

                // Obtenemos información del usuario y la pasamos al modelo
                User user = userRepository.findByEmail(email).orElse(null); // Buscamos el usuario por su correo
                model.addAttribute("usuario", user); // Pasamos al modelo los detalles del usuario autenticado

                // Contamos la cantidad de entidades en cada repositorio y pasamos estos valores al modelo
                model.addAttribute("numUsuarios", userRepository.count()); // Total de usuarios
                model.addAttribute("numMascotas", mascotaRepository.count()); // Total de mascotas
                model.addAttribute("numCitas", citaRepository.count()); // Total de citas
                model.addAttribute("numVeterinarios", veterinarioRepository.count()); // Total de veterinarios

                // Paginación para las mascotas
                Page<Mascota> mascotas;
                if (especie != null && !especie.isEmpty()) { // Si se ha pasado una especie, filtramos las mascotas por especie
                    if ("OTRO".equals(especie)) {
                        mascotas = mascotaRepository.findByEspecieNotIn(Arrays.asList("PERRO", "GATO"), pageable); // Filtramos por especies diferentes a perro y gato
                    } else {
                        mascotas = mascotaRepository.findByEspecieIgnoreCase(especie, pageable); // Filtramos por especie
                    }
                    model.addAttribute("especieFiltro", especie); // Pasamos la especie al modelo
                } else {
                    mascotas = mascotaRepository.findAll(pageable); // Si no se pasa especie, mostramos todas las mascotas
                }
                model.addAttribute("mascotas", mascotas); // Pasamos la lista de mascotas al modelo

                // Paginación para otros recursos (veterinarios, usuarios, dueños y citas)
                Page<Veterinario> veterinarios = veterinarioRepository.findAll(pageable);
                Page<User> usuarios = userRepository.findAll(pageable);
                Page<Dueno> duenos = duenoRepository.findAll(pageable);
                Page<Cita> citas = citaRepository.findAll(pageable);

                // Pasamos los resultados de estas consultas al modelo
                model.addAttribute("veterinarios", veterinarios);
                model.addAttribute("usuarios", usuarios);
                model.addAttribute("duenos", duenos);
                model.addAttribute("citas", citas);
                model.addAttribute("adminEmail", adminEmail); // Correo del administrador para verificar el acceso
                model.addAttribute("especies", List.of("PERRO", "GATO", "OTRO")); // Lista de especies para filtrar
                model.addAttribute("currentTab", tab); // Pestaña actual (por defecto es 'mascotas')

                return "admin"; // Retornamos la vista del panel admin
            }
        }
        return "redirect:/dashboard"; // Si el usuario no es el admin, lo redirigimos al dashboard
    }

    // Método que maneja la visualización de los detalles de una mascota
    @GetMapping("/admin/mascota/{id}")
    public String verDetalleMascota(@AuthenticationPrincipal OAuth2User principal, // Obtiene el usuario autenticado
            @PathVariable Long id, // Captura el ID de la mascota en la URL
            Model model) {

        // Verificamos que el usuario esté autenticado
        if (principal != null) {
            String email = principal.getAttribute("email"); // Obtenemos el correo del usuario autenticado

            // Verificamos si el usuario autenticado es el administrador
            if (adminEmail.equals(email)) {
                Mascota mascota = mascotaRepository.findById(id).orElse(null); // Buscamos la mascota por su ID
                if (mascota != null) { // Si la mascota existe, pasamos los datos al modelo
                    User user = userRepository.findByEmail(email).orElse(null); // Obtenemos el usuario administrador
                    model.addAttribute("usuario", user); // Añadimos el usuario al modelo
                    model.addAttribute("mascota", mascota); // Añadimos la mascota al modelo
                    model.addAttribute("adminEmail", adminEmail); // Añadimos el correo del administrador al modelo
                    return "mascota-detalle"; // Retornamos la vista de detalles de la mascota
                }
            }
        }
        return "redirect:/admin"; // Si no se encuentra la mascota o el usuario no es el admin, redirigimos al panel de administración
    }
}
