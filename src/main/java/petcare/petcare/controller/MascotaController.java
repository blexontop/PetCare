package petcare.petcare.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import jakarta.servlet.http.HttpServletResponse;
import petcare.petcare.model.Dueno;
import petcare.petcare.model.Mascota;
import petcare.petcare.model.User;
import petcare.petcare.model.Cita;
import petcare.petcare.repository.DuenoRepository;
import petcare.petcare.repository.MascotaRepository;
import petcare.petcare.repository.UserRepository;
import petcare.petcare.repository.CitaRepository;
import petcare.petcare.service.DogCatApiService;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MascotaController {

    private final MascotaRepository mascotaRepository;
    private final UserRepository userRepository;
    private final CitaRepository citaRepository;
    private final DogCatApiService dogCatApiService;
    private final DuenoRepository duenoRepository;

    @Value("${APP_ADMIN_EMAIL}")
    private String adminEmail;

    // --- MÉTODOS AUXILIARES ---

    private String getAuthenticatedEmail(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            return oauth2User.getAttribute("email");
        } else if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return null;
    }

    private boolean isAdmin(String email) {
        return adminEmail.equals(email);
    }

    // --- CREACIÓN DE MASCOTA (SE MANTIENE CASI IGUAL) ---

    @GetMapping("/mascotas/nueva")
    public String mostrarFormularioNuevaMascota(Authentication authentication, Model model) {
        String email = getAuthenticatedEmail(authentication);
        if (email == null) return "redirect:/login";

        if (!isAdmin(email)) {
            return "redirect:/dashboard";
        }

        model.addAttribute("usuario", userRepository.findByEmail(email).orElse(null));
        model.addAttribute("duenos", duenoRepository.findAll()); // Solo visible para el admin
        return "mascota-form"; // Usa el formulario que incluye la selección de dueño
    }

    @GetMapping("/mascotas/nueva/personal")
    public String mostrarFormularioNuevaMascotaPersonal(Authentication authentication, Model model) {
        String email = getAuthenticatedEmail(authentication);
        if (email == null) return "redirect:/login";

        model.addAttribute("usuario", userRepository.findByEmail(email).orElse(null));
        // No se añade 'duenos' al modelo para ocultar el selector en el formulario personal
        return "mascota-form-personal"; // Usa el formulario personal
    }

    @PostMapping("/mascotas")
    public String guardarMascota(Authentication authentication,
            @RequestParam String nombre,
            @RequestParam String especie,
            @RequestParam(required = false) String raza,
            @RequestParam(required = false) String fechaNacimiento,
            @RequestParam(required = false) Double peso,
            @RequestParam(required = false) Long duenoId) { // duenoId es opcional en la petición POST
        
        String email = getAuthenticatedEmail(authentication);
        if (email == null) return "redirect:/login";

        Dueno dueno;
        if (isAdmin(email) && duenoId != null) {
            // El admin puede asignar un dueño específico
            dueno = duenoRepository.findById(duenoId).orElse(null);
        } else {
            // Usuario normal o admin sin duenoId asigna el dueño por su propio email
            dueno = duenoRepository.findByEmail(email).orElse(null);
        }

        if (dueno == null) {
            return "redirect:/dashboard?error=SinDuenoAsignado";
        }

        Mascota mascota = Mascota.builder()
                .nombre(nombre)
                .especie(especie)
                .raza(raza)
                .peso(peso)
                .dueno(dueno)
                .build();

        if (fechaNacimiento != null && !fechaNacimiento.isBlank()) {
            mascota.setFechaNacimiento(LocalDate.parse(fechaNacimiento));
        }

        if ("PERRO".equalsIgnoreCase(especie)) {
            mascota.setFotoUrl(dogCatApiService.obtenerImagenPerroAleatoria());
        } else if ("GATO".equalsIgnoreCase(especie)) {
            mascota.setFotoUrl(dogCatApiService.obtenerImagenGatoAleatoria());
        }

        mascotaRepository.save(mascota);
        
        if (isAdmin(email)) {
            return "redirect:/admin";
        }
        return "redirect:/dashboard";
    }

    // --- EDICIÓN DE MASCOTA (MÉTODOS UNIFICADOS) ---

    // Este GET ahora sirve tanto para admin como para el dueño, y se encarga de elegir la vista (form)
    @GetMapping({"/mascotas/{id}/editar", "/mascotas/{id}/editar/personal"})
    public String mostrarFormularioEditarMascota(Authentication authentication, @PathVariable Long id, Model model) {
        String email = getAuthenticatedEmail(authentication);
        if (email == null) return "redirect:/login";

        Mascota mascota = mascotaRepository.findById(id).orElse(null);
        if (mascota == null) {
            return "redirect:/admin?error=MascotaNoEncontrada";
        }
        
        boolean esAdmin = isAdmin(email);
        
        // Verificar autorización (Admin O Dueño)
        if (!esAdmin && (mascota.getDueno() == null || !email.equals(mascota.getDueno().getEmail()))) {
            return "redirect:/admin?error=NoAutorizado";
        }

        model.addAttribute("mascota", mascota);
        User user = userRepository.findByEmail(email).orElse(null);
        model.addAttribute("usuario", user);

        if (esAdmin) {
            // Si es admin, añade la lista de dueños y usa el formulario 'admin'
            model.addAttribute("duenos", duenoRepository.findAll());
            return "mascota-form";
        } else {
            // Si es dueño, usa el formulario 'personal' (oculta el selector de dueño)
            return "mascota-form-personal";
        }
    }

    // Este POST unificado maneja ambas rutas de actualización
    @PostMapping({"/mascotas/{id}/editar", "/mascotas/{id}/editar/personal"})
    public String actualizarMascota(Authentication authentication,
            @PathVariable Long id,
            @RequestParam String nombre,
            @RequestParam String especie,
            @RequestParam(required = false) String raza,
            @RequestParam(required = false) String fechaNacimiento,
            @RequestParam(required = false) Double peso,
            // duenoId solo debe ser requerido si la petición viene del admin (ruta normal)
            // Para unificar, lo hacemos opcional aquí y usamos lógica condicional para el admin
            @RequestParam(required = false) Long duenoId) { 
        
        String email = getAuthenticatedEmail(authentication);
        if (email == null) return "redirect:/login";

        Mascota mascota = mascotaRepository.findById(id).orElse(null);
        if (mascota == null) {
            return "redirect:/admin?error=MascotaNoEncontrada";
        }
        
        boolean esAdmin = isAdmin(email);

        // Verificar autorización (Admin O Dueño)
        if (!esAdmin && (mascota.getDueno() == null || !email.equals(mascota.getDueno().getEmail()))) {
            return "redirect:/dashboard?error=NoAutorizado";
        }

        Dueno nuevoDueno;
        if (esAdmin && duenoId != null) {
            // Si es Admin y se envió un duenoId, usa ese
            nuevoDueno = duenoRepository.findById(duenoId).orElse(null);
        } else if (!esAdmin) {
            // Si es un usuario normal (no admin), el dueño NO se puede cambiar y debe ser él mismo
            nuevoDueno = duenoRepository.findByEmail(email).orElse(null);
        } else {
            // Caso por defecto: Si es admin pero no envió duenoId, mantiene el actual
            nuevoDueno = mascota.getDueno();
        }

        if (nuevoDueno == null) {
            return "redirect:/dashboard?error=DuenoInvalido";
        }

        // Actualizar campos
        mascota.setNombre(nombre);
        mascota.setEspecie(especie);
        mascota.setRaza(raza);
        mascota.setPeso(peso);
        mascota.setDueno(nuevoDueno); // El dueno se asigna según la lógica anterior

        if (fechaNacimiento != null && !fechaNacimiento.isBlank()) {
            mascota.setFechaNacimiento(LocalDate.parse(fechaNacimiento));
        } else {
            mascota.setFechaNacimiento(null);
        }

        // Actualizar imagen
        if ("PERRO".equalsIgnoreCase(especie)) {
            mascota.setFotoUrl(dogCatApiService.obtenerImagenPerroAleatoria());
        } else if ("GATO".equalsIgnoreCase(especie)) {
            mascota.setFotoUrl(dogCatApiService.obtenerImagenGatoAleatoria());
        } else {
            mascota.setFotoUrl(null);
        }

        mascotaRepository.save(mascota);
        
        if (esAdmin) {
            return "redirect:/admin";
        }
        return "redirect:/dashboard";
    }

    // --- ELIMINACIÓN DE MASCOTA (SE MANTIENE IGUAL) ---

    @GetMapping("/mascotas/{id}/eliminar")
    public String eliminarMascota(Authentication authentication, @PathVariable Long id) {
        String email = getAuthenticatedEmail(authentication);
        if (email == null) return "redirect:/login";

        Mascota mascota = mascotaRepository.findById(id).orElse(null);
        if (mascota == null) {
            return "redirect:/admin?error=MascotaNoEncontrada";
        }
        
        boolean esAdmin = isAdmin(email);

        // Permitir eliminar si usuario es admin o dueño de la mascota
        if (!esAdmin && (mascota.getDueno() == null || !email.equals(mascota.getDueno().getEmail()))) {
            String redirectUrl = esAdmin ? "/admin" : "/dashboard";
            return "redirect:" + redirectUrl + "?error=NoAutorizado";
        }

        // Eliminar citas asociadas para evitar violaciones de clave foránea
        List<Cita> citasAsociadas = citaRepository.findByMascota(mascota);
        for (Cita cita : citasAsociadas) {
            citaRepository.deleteById(cita.getId());
        }
        mascotaRepository.deleteById(id);
        
        if (esAdmin) {
            return "redirect:/admin";
        }
        return "redirect:/dashboard";
    }

    // --- MÉTODOS DE EXPORTACIÓN (SE MANTIENEN IGUAL) ---

    // Metodo para exportar mascotas a CSV
    @GetMapping(value = "/export/csv", produces = "text/csv")
    public void exportMascotasCSV(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=mascotas.csv");

        PrintWriter writer = response.getWriter();
        writer.println("id,nombre,especie,raza,fecha_nacimiento,peso,foto_url,dueno_id");

        List<Mascota> mascotas = mascotaRepository.findAll();

        for (Mascota m : mascotas) {
            writer.println(
                    m.getId() + "," +
                    m.getNombre() + "," +
                    m.getEspecie() + "," +
                    m.getRaza() + "," +
                    m.getFechaNacimiento() + "," +
                    m.getPeso() + "," +
                    m.getFotoUrl() + "," +
                    (m.getDueno() != null ? m.getDueno().getId() : ""));
        }

        writer.flush();
        writer.close();
    }

    // Metodo para exportar mascotas a PDF
    @GetMapping(value = "/export/pdf", produces = "application/pdf")
    public void exportMascotasPDF(HttpServletResponse response) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=mascotas.pdf");

        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();
        document.add(new Paragraph("Lista de Mascotas"));
        document.add(new Paragraph(" "));

        List<Mascota> mascotas = mascotaRepository.findAll();

        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);

        table.addCell("ID");
        table.addCell("Nombre");
        table.addCell("Especie");
        table.addCell("Raza");
        table.addCell("Nacimiento");
        table.addCell("Peso");
        table.addCell("Foto URL");
        table.addCell("Dueño ID");

        for (Mascota m : mascotas) {
            table.addCell(String.valueOf(m.getId()));
            table.addCell(m.getNombre());
            table.addCell(m.getEspecie());
            table.addCell(m.getRaza());
            table.addCell(m.getFechaNacimiento() != null ? m.getFechaNacimiento().toString() : "");
            table.addCell(m.getPeso() != null ? m.getPeso().toString() : "");
            table.addCell(m.getFotoUrl() != null ? m.getFotoUrl() : "");
            table.addCell(m.getDueno() != null ? String.valueOf(m.getDueno().getId()) : "");
        }

        document.add(table);
        document.close();
    }
}