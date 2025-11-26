package petcare.petcare.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal; 
import org.springframework.security.crypto.password.PasswordEncoder; 
import org.springframework.security.oauth2.core.user.OAuth2User; 
import org.springframework.beans.factory.annotation.Value; 
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails; 
import org.springframework.stereotype.Controller; 
import org.springframework.ui.Model; 
import org.springframework.web.bind.annotation.GetMapping; 
import org.springframework.web.bind.annotation.ModelAttribute; 
import org.springframework.web.bind.annotation.PostMapping; 
import org.springframework.web.bind.annotation.RequestParam; 
import org.springframework.web.servlet.mvc.support.RedirectAttributes; 

// import petcare.petcare.model.AuthProvider; // Modelos de autenticación (comentado para evitar warning si no se usa)
import petcare.petcare.model.Dueno; 
import petcare.petcare.model.Mascota; 
import petcare.petcare.model.User; 
import petcare.petcare.repository.DuenoRepository; 
import petcare.petcare.repository.MascotaRepository; 
import petcare.petcare.repository.UserRepository;
import petcare.petcare.service.EmailService; 

import java.time.LocalDateTime; 
import java.util.List; 
import jakarta.servlet.http.HttpSession; 

@Controller // Indicamos que esta clase es un controlador
@RequiredArgsConstructor // Lombok genera un constructor con todas las dependencias final
public class MainController {

    private final UserRepository userRepository; // Repositorio de Usuarios
    private final DuenoRepository duenoRepository; // Repositorio de Dueños
    private final MascotaRepository mascotaRepository; // Repositorio de Mascotas
    private final EmailService emailService; // Servicio de correos electrónicos
    private final PasswordEncoder passwordEncoder; // Para encriptar contraseñas

    @Value("${APP_ADMIN_EMAIL}") // Leemos la dirección de correo del administrador desde application.properties
    private String adminEmail;

    // Método que maneja la vista principal
    @GetMapping("/") 
    public String home(Authentication authentication, HttpSession session, Model model) {
        User user = null; // Inicializamos la variable user
        boolean isGuest = session.getAttribute("isGuest") != null && (boolean) session.getAttribute("isGuest"); // Verificamos si es un invitado

        // Si el usuario está autenticado
        if (authentication != null && authentication.isAuthenticated()) {
            // Si es un invitado, eliminamos la bandera de invitado
            if (isGuest) {
                session.removeAttribute("isGuest");
                isGuest = false;
            }

            Object principal = authentication.getPrincipal(); // Obtenemos al usuario autenticado
            if (principal instanceof OAuth2User oauth2User) { // Si es un usuario OAuth2 (Google, GitHub)
                String email = oauth2User.getAttribute("email"); // Obtenemos el correo electrónico
                if (email != null) {
                    user = userRepository.findByEmail(email).orElse(null); // Buscamos al usuario por su email
                }
            } else if (principal instanceof UserDetails userDetails) { // Si es un usuario normal
                String email = userDetails.getUsername(); // Obtenemos el email del usuario
                if (email != null) {
                    user = userRepository.findByEmail(email).orElse(null); // Buscamos al usuario por su email
                }
            }

            model.addAttribute("isGuest", false); // No es un invitado
        } else if (isGuest) { // Si es un invitado
            model.addAttribute("isGuest", true);
            model.addAttribute("usuarioNombre", "Invitado"); // Mostramos "Invitado" en la interfaz
        } else {
            model.addAttribute("isGuest", false); // No es invitado
        }

        model.addAttribute("usuario", user); // Enviamos el usuario a la vista
        return "home"; // Retornamos la vista 'home'
    }

    // Método que maneja el login
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("loginError", true); // Si hay error, lo mostramos en la vista
        }
        return "login"; // Retornamos la vista 'login'
    }

    // Método para login de invitado
    @GetMapping("/guest-login")
    public String guestLogin(HttpSession session) {
        session.setAttribute("isGuest", true); // Marcamos al usuario como invitado
        return "redirect:/mapa"; // Redirigimos al mapa
    }

    // Método que muestra el formulario de registro
    @GetMapping("/register")
    public String showRegistrationForm() {
        return "registration"; // Retornamos la vista de registro
    }

    // Método POST que maneja el registro de un nuevo usuario
    @PostMapping("/register")
    public String registerUser(@RequestParam("email") String email,
            @RequestParam("name") String name,
            @RequestParam("password") String password,
            RedirectAttributes redirectAttributes) {

        if (userRepository.findByEmail(email).isPresent()) { // Si el email ya está registrado
            redirectAttributes.addFlashAttribute("error", "El correo ya está registrado");
            return "redirect:/register"; // Redirigimos al formulario de registro
        }

        User user = User.builder() // Creamos un nuevo objeto usuario
                .email(email)
                .name(name)
                .password(passwordEncoder.encode(password)) // Encriptamos la contraseña
                .provider(null) // Proveedor de autenticación local
                .createdAt(LocalDateTime.now()) // Fecha de creación
                .updatedAt(LocalDateTime.now()) // Fecha de actualización
                .build();

        userRepository.save(user); // Guardamos el usuario

        // Crear también un registro en Dueno
        Dueno dueno = Dueno.builder() // Creamos un objeto Dueno
                .nombre(name)
                .email(email)
                .build();
        duenoRepository.save(dueno); // Guardamos el dueño

        // Enviar un email de bienvenida
        emailService.sendWelcomeEmail(email, name);

        redirectAttributes.addFlashAttribute("success", "Registro exitoso. Ahora puedes iniciar sesión.");
        return "redirect:/login"; // Redirigimos al login
    }

    // Método que muestra el dashboard
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = null;
        List<Mascota> mascotas = List.of();

        if (authentication == null || !authentication.isAuthenticated()) { // Si no está autenticado
            return "redirect:/login"; // Redirigimos al login
        }

        // Obtención de usuario autenticado y sus mascotas
        Object principal = authentication.getPrincipal();
        String email = null;
        String name = null;
        String picture = null;
        boolean isAdmin = false; // Guardamos si el usuario es administrador

        // Aquí procesamos la información dependiendo si es un usuario OAuth2 o normal
        if (principal instanceof OAuth2User oauth2User) {
            email = oauth2User.getAttribute("email");
            name = oauth2User.getAttribute("name");
            picture = oauth2User.getAttribute("picture");
            // Fallback si no obtenemos el email
            if (email == null && oauth2User.getAttributes().containsKey("email")) {
                email = oauth2User.getAttribute("email");
            }
            if (email == null && oauth2User.getAttribute("login") != null) {
                email = oauth2User.getAttribute("login") + "@github.local"; // Fallback para GitHub
            }

            if (email != null) {
                user = userRepository.findByEmail(email).orElse(null);
                Dueno dueno = duenoRepository.findByEmail(email).orElse(null);
                isAdmin = adminEmail.equals(email); // Verificamos si el usuario es admin
                if (dueno != null) {
                    mascotas = mascotaRepository.findByDueno(dueno); // Obtenemos las mascotas del dueño
                }
            }
        } else if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername(); // Para un usuario normal
            if (email != null) {
                email = email.trim().toLowerCase(); // Normalizamos el email
                user = userRepository.findByEmail(email).orElse(null);
            }
        }

        model.addAttribute("usuario", user);
        model.addAttribute("usuarioNombre", name); // Nombre para la vista (si proviene de OAuth2)
        model.addAttribute("picture", picture); // Foto/Avatar del proveedor OAuth2
        model.addAttribute("isAdmin", isAdmin); // Indicador para vistas
        model.addAttribute("mascotas", mascotas); // Añadimos las mascotas al modelo
        model.addAttribute("adminEmail", adminEmail); // Añadimos el email del admin
        return "dashboard"; // Retornamos la vista 'dashboard'
    }

    // Método POST que permite completar el perfil de un dueño
    @PostMapping("/dueno/completar")
    public String completarPerfilDueno(@ModelAttribute Dueno duenoForm,
            @AuthenticationPrincipal OAuth2User principal,
            RedirectAttributes redirectAttributes) {
        if (principal != null) {
            String email = principal.getAttribute("email");

            // Procesamos la información del usuario y actualizamos su perfil
            if (email != null) {
                Dueno dueno = duenoRepository.findByEmail(email).orElse(null);
                User user = userRepository.findByEmail(email).orElse(null);
                if (dueno != null && user != null) {
                    dueno.setNombre(duenoForm.getNombre());
                    dueno.setTelefono(duenoForm.getTelefono());
                    dueno.setDireccion(duenoForm.getDireccion());
                    dueno.setCiudad(duenoForm.getCiudad());

                    if (duenoForm.getPassword() != null && !duenoForm.getPassword().isEmpty()) {
                        user.setPassword(passwordEncoder.encode(duenoForm.getPassword()));
                    }

                    user.setName(duenoForm.getNombre());
                    duenoRepository.save(dueno);
                    userRepository.save(user);

                    redirectAttributes.addFlashAttribute("success", "Perfil completado exitosamente");
                    return "redirect:/dashboard";
                }
            }
        }
        redirectAttributes.addFlashAttribute("error", "Error al completar el perfil");
        return "redirect:/dashboard";
    }

    // Método para mostrar el mapa
    @GetMapping("/mapa")
    public String mapa(Authentication authentication, HttpSession session, Model model) {
        User user = null;
        boolean isGuest = session.getAttribute("isGuest") != null && (boolean) session.getAttribute("isGuest");

        if (authentication != null && authentication.isAuthenticated()) {
            // Si está autenticado, obtenemos los detalles del usuario
            Object principal = authentication.getPrincipal();
            if (principal instanceof OAuth2User oauth2User) {
                String email = oauth2User.getAttribute("email");
                if (email != null) {
                    user = userRepository.findByEmail(email).orElse(null);
                }
            } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                String email = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                if (email != null) {
                    user = userRepository.findByEmail(email).orElse(null);
                }
            }

            model.addAttribute("isGuest", false);
            model.addAttribute("usuario", user);
            return "mapa"; // Retornamos la vista 'mapa'
        }

        // Si no autenticado, solo permitimos acceso a invitados
        if (isGuest) {
            model.addAttribute("isGuest", true);
            model.addAttribute("usuarioNombre", "Invitado");
            return "mapa"; // Retornamos la vista 'mapa' para invitados
        }

        return "redirect:/login"; // Si no es invitado ni autenticado, redirigimos al login
    }

}
