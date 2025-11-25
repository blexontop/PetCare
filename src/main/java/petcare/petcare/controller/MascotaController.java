package petcare.petcare.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import petcare.petcare.repository.DuenoRepository;
import petcare.petcare.repository.MascotaRepository;
import petcare.petcare.repository.UserRepository;
import petcare.petcare.service.DogCatApiService;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MascotaController {

    private final MascotaRepository mascotaRepository;
    private final DuenoRepository duenoRepository;
    private final DogCatApiService dogCatApiService;
    private final UserRepository userRepository;

    @Value("${APP_ADMIN_EMAIL}")
    private String adminEmail;

    @GetMapping("/mascotas/nueva")
    public String mostrarFormularioNuevaMascota(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = null;
        Object principal = authentication.getPrincipal();

        if (principal instanceof OAuth2User oauth2User) {
            email = oauth2User.getAttribute("email");
        } else if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        }

        if (email == null) {
            return "redirect:/login";
        }

        if (!adminEmail.equals(email)) {
            return "redirect:/dashboard";
        }

        model.addAttribute("usuario", userRepository.findByEmail(email).orElse(null));
        model.addAttribute("duenos", duenoRepository.findAll());
        model.addAttribute("isPersonal", false);
        return "mascota-form";
    }

    @GetMapping("/mascotas/nueva/personal")
    public String mostrarFormularioNuevaMascotaPersonal(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = null;
        Object principal = authentication.getPrincipal();

        if (principal instanceof OAuth2User oauth2User) {
            email = oauth2User.getAttribute("email");
        } else if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        }

        if (email == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuario", userRepository.findByEmail(email).orElse(null));
        model.addAttribute("isPersonal", true);
        // no duenos attribute, so owner select hidden
        return "mascota-form-personal";
    }

    @PostMapping("/mascotas")
    public String guardarMascota(Authentication authentication,
            @RequestParam String nombre,
            @RequestParam String especie,
            @RequestParam(required = false) String raza,
            @RequestParam(required = false) String fechaNacimiento,
            @RequestParam(required = false) Double peso,
            @RequestParam(required = false) Long duenoId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String email = null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            email = oauth2User.getAttribute("email");
        } else if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        }
        if (email == null) {
            return "redirect:/login";
        }

        Dueno dueno;
        if (adminEmail.equals(email) && duenoId != null) {
            dueno = duenoRepository.findById(duenoId).orElse(null);
        } else {
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
        if (adminEmail.equals(email)) {
            return "redirect:/admin";
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/mascotas/{id}/eliminar")
    public String eliminarMascota(Authentication authentication, @PathVariable Long id) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            email = oauth2User.getAttribute("email");
        } else if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        }

        if (email == null) {
            return "redirect:/login";
        }

        Mascota mascota = mascotaRepository.findById(id).orElse(null);
        if (mascota == null) {
            // Mascota no encontrada, redirigir con error
            return "redirect:/admin?error=MascotaNoEncontrada";
        }
        // Permitir eliminar si usuario es admin o dueño de la mascota
        if (!adminEmail.equals(email) && (mascota.getDueno() == null || !email.equals(mascota.getDueno().getEmail()))) {
            // No autorizado para eliminar
            return "redirect:/admin?error=NoAutorizado";
        }

        mascotaRepository.deleteById(id);
        return "redirect:/admin";
    }

    @GetMapping("/mascotas/{id}/editar")
    public String mostrarFormularioEditarMascota(Authentication authentication, @PathVariable Long id,
            Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            email = oauth2User.getAttribute("email");
        } else if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        }

        if (email == null) {
            return "redirect:/login";
        }

        Mascota mascota = mascotaRepository.findById(id).orElse(null);
        if (mascota == null) {
            return "redirect:/admin?error=MascotaNoEncontrada";
        }
        // Permitir editar si usuario es admin o dueño
        if (!adminEmail.equals(email) && (mascota.getDueno() == null || !email.equals(mascota.getDueno().getEmail()))) {
            return "redirect:/admin?error=NoAutorizado";
        }

        model.addAttribute("mascota", mascota);
        model.addAttribute("duenos", duenoRepository.findAll());
        User user = userRepository.findByEmail(email).orElse(null);
        model.addAttribute("usuario", user);
        return "mascota-form";
    }

    @PostMapping("/mascotas/{id}/editar")
    public String actualizarMascota(Authentication authentication,
            @PathVariable Long id,
            @RequestParam String nombre,
            @RequestParam String especie,
            @RequestParam(required = false) String raza,
            @RequestParam(required = false) String fechaNacimiento,
            @RequestParam(required = false) Double peso,
            @RequestParam Long duenoId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            email = oauth2User.getAttribute("email");
        } else if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        }

        if (email == null) {
            return "redirect:/login";
        }

        Mascota mascota = mascotaRepository.findById(id).orElse(null);
        if (mascota == null) {
            return "redirect:/admin?error=MascotaNoEncontrada";
        }
        // Permitir actualizar si usuario es admin o dueño
        if (!adminEmail.equals(email) && (mascota.getDueno() == null || !email.equals(mascota.getDueno().getEmail()))) {
            return "redirect:/admin?error=NoAutorizado";
        }

        Dueno dueno = duenoRepository.findById(duenoId).orElse(null);

        // Actualizar campos
        mascota.setNombre(nombre);
        mascota.setEspecie(especie);
        mascota.setRaza(raza);
        mascota.setPeso(peso);
        mascota.setDueno(dueno);

        if (fechaNacimiento != null && !fechaNacimiento.isBlank()) {
            mascota.setFechaNacimiento(LocalDate.parse(fechaNacimiento));
        } else {
            mascota.setFechaNacimiento(null);
        }

        // Actualizar imagen de Dog/Cat API basada en especie (opcional, puede omitirse
        // si se quieren mantener fotos)
        if ("PERRO".equalsIgnoreCase(especie)) {
            mascota.setFotoUrl(dogCatApiService.obtenerImagenPerroAleatoria());
        } else if ("GATO".equalsIgnoreCase(especie)) {
            mascota.setFotoUrl(dogCatApiService.obtenerImagenGatoAleatoria());
        } else {
            mascota.setFotoUrl(null);
        }

        mascotaRepository.save(mascota);
        return "redirect:/admin";
    }

    // Metodo para exportar mascotas a CSV
    @GetMapping(value = "/export/csv", produces = "text/csv")
    public void exportMascotasCSV(HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=mascotas.csv");

        PrintWriter writer = response.getWriter();

        // Encabezado CSV
        writer.println("id,nombre,especie,raza,fecha_nacimiento,peso,foto_url,dueno_id");

        // Obtener mascotas de BD
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

        // Crear tabla con 8 columnas
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);

        // Encabezados
        table.addCell("ID");
        table.addCell("Nombre");
        table.addCell("Especie");
        table.addCell("Raza");
        table.addCell("Nacimiento");
        table.addCell("Peso");
        table.addCell("Foto URL");
        table.addCell("Dueño ID");

        // Filas
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

        // Añadir tabla al documento
        document.add(table);

        document.close();
    }

}
