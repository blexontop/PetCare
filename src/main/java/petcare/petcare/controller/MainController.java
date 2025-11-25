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

import petcare.petcare.model.AuthProvider;
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

@Controller
@RequiredArgsConstructor
public class MainController {

    private final UserRepository userRepository;
    private final DuenoRepository duenoRepository;
    private final MascotaRepository mascotaRepository;
    private final EmailService emailService; // 👈 inyectamos EmailService
    private final PasswordEncoder passwordEncoder;

    @Value("${APP_ADMIN_EMAIL}")
    private String adminEmail;

    @GetMapping("/")
    public String home(Authentication authentication, HttpSession session, Model model) {
        User user = null;
        boolean isGuest = session.getAttribute("isGuest") != null && (boolean) session.getAttribute("isGuest");

        // If authenticated, prefer authenticated user and clear guest flag
        if (authentication != null && authentication.isAuthenticated()) {
            // remove guest flag if present
            if (isGuest) {
                session.removeAttribute("isGuest");
                isGuest = false;
            }

            Object principal = authentication.getPrincipal();
            if (principal instanceof OAuth2User oauth2User) {
                String email = oauth2User.getAttribute("email");
                if (email != null) {
                    user = userRepository.findByEmail(email).orElse(null);
                }
            } else if (principal instanceof UserDetails userDetails) {
                String email = userDetails.getUsername();
                if (email != null) {
                    user = userRepository.findByEmail(email).orElse(null);
                }
            }

            model.addAttribute("isGuest", false);
        } else if (isGuest) {
            model.addAttribute("isGuest", true);
            model.addAttribute("usuarioNombre", "Invitado");
        } else {
            model.addAttribute("isGuest", false);
        }

        model.addAttribute("usuario", user);
        return "home";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("loginError", true);
        }
        return "login";
    }

    @GetMapping("/guest-login")
    public String guestLogin(HttpSession session) {
        session.setAttribute("isGuest", true);
        return "redirect:/mapa";
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "registration";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam("email") String email,
            @RequestParam("name") String name,
            @RequestParam("password") String password,
            RedirectAttributes redirectAttributes) {

        if (userRepository.findByEmail(email).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "El correo ya está registrado");
            return "redirect:/register";
        }

        User user = User.builder()
                .email(email)
                .name(name)
                .password(passwordEncoder.encode(password))
                .provider(null) // local provider
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        // Crear también un registro en Dueno básico
        Dueno dueno = Dueno.builder()
                .nombre(name)
                .email(email)
                .build();
        duenoRepository.save(dueno);

        // Enviar email de bienvenida
        emailService.sendWelcomeEmail(email, name);

        redirectAttributes.addFlashAttribute("success", "Registro exitoso. Ahora puedes iniciar sesión.");
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = null;
        List<Mascota> mascotas = List.of();

        if (authentication == null || !authentication.isAuthenticated()) {
            // If not authenticated, redirect to login
            return "redirect:/login";
        }

        Object principal = authentication.getPrincipal();
        String email = null;
        String name = null;
        String picture = null;

        if (principal instanceof OAuth2User oauth2User) {
            email = oauth2User.getAttribute("email");
            name = oauth2User.getAttribute("name");
            picture = oauth2User.getAttribute("picture");
            String login = oauth2User.getAttribute("login");

            // For GitHub, email might be null initially, try to get it from attributes
            if (email == null && oauth2User.getAttributes().containsKey("email")) {
                email = oauth2User.getAttribute("email");
            }

            // If still no email, use GitHub login as fallback
            if (email == null && login != null) {
                email = login + "@github.local"; // Temporary email for GitHub users without public email
            }

            if (email != null) {
                // Busca o crea el usuario OAuth2
                user = userRepository.findByEmail(email).orElse(null);
                if (user == null) {
                    AuthProvider provider = AuthProvider.GOOGLE;
                    if (login != null || oauth2User.getAttributes().containsKey("id")) {
                        provider = AuthProvider.GITHUB;
                    }
                    user = User.builder()
                            .email(email)
                            .name(name != null ? name : (login != null ? login : email))
                            .picture(picture)
                            .provider(provider)
                            .createdAt(java.time.LocalDateTime.now())
                            .updatedAt(java.time.LocalDateTime.now())
                            .build();
                    user = userRepository.save(user);

                    Dueno dueno = Dueno.builder()
                            .nombre(name != null ? name : (login != null ? login : email))
                            .email(email)
                            .build();
                    duenoRepository.save(dueno);

                    emailService.sendWelcomeEmail(user.getEmail(), user.getName());

                    model.addAttribute("dueno", dueno);
                    return "dueno-form";
                } else {
                    Dueno dueno = duenoRepository.findByEmail(email).orElse(null);
                    boolean isAdmin = adminEmail.equals(email);
                    if (dueno != null) {
                        // Require user to complete dueno details only if they are not admin
                        if (!isAdmin && (dueno.getTelefono() == null || dueno.getTelefono().isEmpty() ||
                                dueno.getDireccion() == null || dueno.getDireccion().isEmpty() ||
                                dueno.getCiudad() == null || dueno.getCiudad().isEmpty())) {
                            model.addAttribute("dueno", dueno);
                            return "dueno-form";
                        }
                        if (isAdmin) {
                            // Admin only sees their own mascotas in dashboard, all mascotas in admin panel
                            mascotas = mascotaRepository.findByDueno(dueno);
                        } else {
                            mascotas = mascotaRepository.findByDueno(dueno);
                        }
                    }
                }
            }

        } else if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
            if (email != null) {
                email = email.trim().toLowerCase(); // Normalize email
                user = userRepository.findByEmail(email).orElse(null);
            }
            Dueno dueno = duenoRepository.findByEmail(email).orElse(null);
            if (dueno != null) {
                boolean isAdmin = adminEmail.equals(email);
                if (isAdmin) {
                    mascotas = mascotaRepository.findByDueno(dueno);
                } else {
                    mascotas = mascotaRepository.findByDueno(dueno);
                }
            }
        }

        model.addAttribute("usuario", user);
        model.addAttribute("mascotas", mascotas);
        model.addAttribute("adminEmail", adminEmail);
        return "dashboard";
    }

    @PostMapping("/dueno/completar")
    public String completarPerfilDueno(@ModelAttribute Dueno duenoForm,
            @AuthenticationPrincipal OAuth2User principal,
            RedirectAttributes redirectAttributes) {
        if (principal != null) {
            String email = principal.getAttribute("email");
            String login = principal.getAttribute("login"); // GitHub username

            // For GitHub, email might be null initially, try to get it from attributes
            if (email == null && principal.getAttributes().containsKey("email")) {
                email = principal.getAttribute("email");
            }

            // If still no email, use GitHub login as fallback
            if (email == null && login != null) {
                email = login + "@github.local"; // Temporary email for GitHub users without public email
            }

            if (email != null) {
                Dueno dueno = duenoRepository.findByEmail(email).orElse(null);
                User user = userRepository.findByEmail(email).orElse(null);
                if (dueno != null && user != null) {
                    dueno.setNombre(duenoForm.getNombre());
                    dueno.setTelefono(duenoForm.getTelefono());
                    dueno.setDireccion(duenoForm.getDireccion());
                    dueno.setCiudad(duenoForm.getCiudad());

                    // Update password only if provided
                    if (duenoForm.getPassword() != null && !duenoForm.getPassword().isEmpty()) {
                        user.setPassword(passwordEncoder.encode(duenoForm.getPassword()));
                    }

                    // Update user's name also
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

    @GetMapping("/mapa")
    public String mapa(Authentication authentication, HttpSession session, Model model) {
        User user = null;
        boolean isGuest = session.getAttribute("isGuest") != null && (boolean) session.getAttribute("isGuest");
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated();

        // Si está autenticado, priorizar la autenticación y limpiar el flag de invitado
        if (isAuthenticated) {
            if (isGuest) {
                session.removeAttribute("isGuest");
                isGuest = false;
            }

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
            return "mapa";
        }

        // Si no autenticado, permitir acceso solo si es invitado
        if (isGuest) {
            model.addAttribute("isGuest", true);
            model.addAttribute("usuarioNombre", "Invitado");
            return "mapa";
        }

        return "redirect:/login";
    }

}
