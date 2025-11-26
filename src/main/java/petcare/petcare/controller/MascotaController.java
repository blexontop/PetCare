package petcare.petcare.controller;

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

@Controller // Indicamos que esta clase es un controlador de Spring
@RequiredArgsConstructor // Lombok genera un constructor con todas las dependencias 'final'
public class MascotaController {

    // Repositorios necesarios para interactuar con la base de datos
    private final MascotaRepository mascotaRepository;
    private final UserRepository userRepository;
    private final CitaRepository citaRepository;
    private final DogCatApiService dogCatApiService;
    private final DuenoRepository duenoRepository;

    // Leemos el correo del administrador desde el archivo de propiedades
    @Value("${APP_ADMIN_EMAIL}")
    private String adminEmail;

    // --- MÉTODOS AUXILIARES --- 

    // Método que obtiene el correo del usuario autenticado
    private String getAuthenticatedEmail(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null; // Si no está autenticado, retornamos null
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            return oauth2User.getAttribute("email"); // Si es OAuth2, obtenemos el email
        } else if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername(); // Si es un usuario normal, obtenemos el email
        }
        return null; // Si no se puede obtener el email, retornamos null
    }

    // Método para verificar si el usuario es administrador
    private boolean isAdmin(String email) {
        return adminEmail.equals(email); // Comparamos el email con el del administrador
    }

    // --- CREACIÓN DE MASCOTA ---

    // Mostrar formulario para crear una nueva mascota (solo para admins)
    @GetMapping("/mascotas/nueva")
    public String mostrarFormularioNuevaMascota(Authentication authentication, Model model) {
        String email = getAuthenticatedEmail(authentication);
        if (email == null) return "redirect:/login"; // Si no está autenticado, redirige al login

        if (!isAdmin(email)) {
            return "redirect:/dashboard"; // Si no es admin, lo redirige al dashboard
        }

        // Si es admin, mostramos el formulario y pasamos la lista de dueños
        model.addAttribute("usuario", userRepository.findByEmail(email).orElse(null)); 
        model.addAttribute("duenos", duenoRepository.findAll()); // Lista de dueños (solo visible para admin)
        return "mascota-form"; // Vista para el formulario de nueva mascota
    }

    // Mostrar formulario para crear una nueva mascota (solo para el dueño)
    @GetMapping("/mascotas/nueva/personal")
    public String mostrarFormularioNuevaMascotaPersonal(Authentication authentication, Model model) {
        String email = getAuthenticatedEmail(authentication);
        if (email == null) return "redirect:/login"; // Si no está autenticado, redirige al login

        // Si es un dueño, mostramos el formulario personal sin la lista de dueños
        model.addAttribute("usuario", userRepository.findByEmail(email).orElse(null));
        return "mascota-form-personal"; // Vista para el formulario personal de mascota
    }

    // Guardar una nueva mascota (POST)
    @PostMapping("/mascotas")
    public String guardarMascota(Authentication authentication,
            @RequestParam String nombre,
            @RequestParam String especie,
            @RequestParam(required = false) String raza,
            @RequestParam(required = false) String fechaNacimiento,
            @RequestParam(required = false) Double peso,
            @RequestParam(required = false) Long duenoId) { // El duenoId es opcional

        String email = getAuthenticatedEmail(authentication);
        if (email == null) return "redirect:/login"; // Si no está autenticado, redirige al login

        Dueno dueno;
        if (isAdmin(email) && duenoId != null) {
            // El admin puede asignar un dueño específico
            dueno = duenoRepository.findById(duenoId).orElse(null);
        } else {
            // Un usuario normal asigna su propio email como dueño
            dueno = duenoRepository.findByEmail(email).orElse(null);
        }

        if (dueno == null) {
            return "redirect:/dashboard?error=SinDuenoAsignado"; // Si no se encuentra un dueño, mostramos un error
        }

        // Crear la mascota
        Mascota mascota = Mascota.builder()
                .nombre(nombre)
                .especie(especie)
                .raza(raza)
                .peso(peso)
                .dueno(dueno)
                .build();

        // Si se ha proporcionado fecha de nacimiento, la asignamos
        if (fechaNacimiento != null && !fechaNacimiento.isBlank()) {
            mascota.setFechaNacimiento(LocalDate.parse(fechaNacimiento));
        }

        // Asignar una foto aleatoria de un perro o un gato según la especie
        if ("PERRO".equalsIgnoreCase(especie)) {
            mascota.setFotoUrl(dogCatApiService.obtenerImagenPerroAleatoria());
        } else if ("GATO".equalsIgnoreCase(especie)) {
            mascota.setFotoUrl(dogCatApiService.obtenerImagenGatoAleatoria());
        }

        mascotaRepository.save(mascota); // Guardar la mascota en la base de datos
        
        if (isAdmin(email)) {
            return "redirect:/admin"; // Si es admin, redirige al panel de admin
        }
        return "redirect:/dashboard"; // Si es un dueño, redirige al dashboard
    }

    // --- EDICIÓN DE MASCOTA ---

    // Mostrar formulario para editar una mascota (admin o dueño)
    @GetMapping({"/mascotas/{id}/editar", "/mascotas/{id}/editar/personal"})
    public String mostrarFormularioEditarMascota(Authentication authentication, @PathVariable Long id, Model model) {
        String email = getAuthenticatedEmail(authentication);
        if (email == null) return "redirect:/login"; // Si no está autenticado, redirige al login

        Mascota mascota = mascotaRepository.findById(id).orElse(null); // Obtener la mascota por ID
        if (mascota == null) {
            return "redirect:/admin?error=MascotaNoEncontrada"; // Si no se encuentra la mascota, mostramos error
        }
        
        boolean esAdmin = isAdmin(email);
        
        // Verificar si el usuario es admin o dueño de la mascota
        if (!esAdmin && (mascota.getDueno() == null || !email.equals(mascota.getDueno().getEmail()))) {
            return "redirect:/admin?error=NoAutorizado"; // Si no tiene permisos, mostramos error
        }

        model.addAttribute("mascota", mascota); // Enviamos la mascota a la vista
        User user = userRepository.findByEmail(email).orElse(null); // Obtenemos el usuario
        model.addAttribute("usuario", user); // Enviamos el usuario a la vista

        if (esAdmin) {
            model.addAttribute("duenos", duenoRepository.findAll()); // Si es admin, pasamos la lista de dueños
            return "mascota-form"; // Formulario de admin
        } else {
            return "mascota-form-personal"; // Si es dueño, mostramos formulario personal
        }
    }

    // Guardar cambios de la mascota (POST)
    @PostMapping({"/mascotas/{id}/editar", "/mascotas/{id}/editar/personal"})
    public String actualizarMascota(Authentication authentication,
            @PathVariable Long id,
            @RequestParam String nombre,
            @RequestParam String especie,
            @RequestParam(required = false) String raza,
            @RequestParam(required = false) String fechaNacimiento,
            @RequestParam(required = false) Double peso,
            @RequestParam(required = false) Long duenoId) { 
        
        String email = getAuthenticatedEmail(authentication);
        if (email == null) return "redirect:/login"; // Si no está autenticado, redirige al login

        Mascota mascota = mascotaRepository.findById(id).orElse(null); // Obtener la mascota por ID
        if (mascota == null) {
            return "redirect:/admin?error=MascotaNoEncontrada"; // Si no se encuentra la mascota, error
        }
        
        boolean esAdmin = isAdmin(email); // Verificamos si es admin

        // Verificar autorización (admin o dueño)
        if (!esAdmin && (mascota.getDueno() == null || !email.equals(mascota.getDueno().getEmail()))) {
            return "redirect:/dashboard?error=NoAutorizado"; // Si no tiene permisos, error
        }

        Dueno nuevoDueno;
        if (esAdmin && duenoId != null) {
            nuevoDueno = duenoRepository.findById(duenoId).orElse(null); // Si es admin y se pasa un duenoId, asignamos ese dueño
        } else if (!esAdmin) {
            nuevoDueno = duenoRepository.findByEmail(email).orElse(null); // Si es un dueño normal, se asigna su propio email
        } else {
            nuevoDueno = mascota.getDueno(); // Si es admin pero no se pasó un duenoId, mantenemos el actual
        }

        if (nuevoDueno == null) {
            return "redirect:/dashboard?error=DuenoInvalido"; // Si no hay dueño válido, error
        }

        // Actualizar los campos de la mascota
        mascota.setNombre(nombre);
        mascota.setEspecie(especie);
        mascota.setRaza(raza);
        mascota.setPeso(peso);
        mascota.setDueno(nuevoDueno); // Asignamos el dueño

        // Si se ha pasado una fecha de nacimiento, la asignamos
        if (fechaNacimiento != null && !fechaNacimiento.isBlank()) {
            mascota.setFechaNacimiento(LocalDate.parse(fechaNacimiento));
        } else {
            mascota.setFechaNacimiento(null); // Si no, dejamos la fecha en null
        }

        // Asignamos imagen dependiendo de la especie
        if ("PERRO".equalsIgnoreCase(especie)) {
            mascota.setFotoUrl(dogCatApiService.obtenerImagenPerroAleatoria());
        } else if ("GATO".equalsIgnoreCase(especie)) {
            mascota.setFotoUrl(dogCatApiService.obtenerImagenGatoAleatoria());
        } else {
            mascota.setFotoUrl(null); // Si no es perro ni gato, asignamos null
        }

        mascotaRepository.save(mascota); // Guardamos la mascota con los cambios

        if (esAdmin) {
            return "redirect:/admin"; // Si es admin, redirige al panel admin
        }
        return "redirect:/dashboard"; // Si es dueño, redirige al dashboard
    }

    // --- ELIMINACIÓN DE MASCOTA --- 

    // Método para eliminar una mascota
    @GetMapping("/mascotas/{id}/eliminar")
    public String eliminarMascota(Authentication authentication, @PathVariable Long id) {
        String email = getAuthenticatedEmail(authentication);
        if (email == null) return "redirect:/login"; // Si no está autenticado, redirige al login

        Mascota mascota = mascotaRepository.findById(id).orElse(null); // Obtenemos la mascota por ID
        if (mascota == null) {
            return "redirect:/admin?error=MascotaNoEncontrada"; // Si no existe, mostramos error
        }

        boolean esAdmin = isAdmin(email); // Verificamos si es admin

        // Permitir eliminar si el usuario es admin o dueño de la mascota
        if (!esAdmin && (mascota.getDueno() == null || !email.equals(mascota.getDueno().getEmail()))) {
            String redirectUrl = esAdmin ? "/admin" : "/dashboard";
            return "redirect:" + redirectUrl + "?error=NoAutorizado"; // Si no tiene permisos, error
        }

        // Eliminar todas las citas asociadas a esta mascota para evitar violaciones de clave foránea
        List<Cita> citasAsociadas = citaRepository.findByMascota(mascota);
        for (Cita cita : citasAsociadas) {
            citaRepository.deleteById(cita.getId()); // Eliminamos las citas
        }
        mascotaRepository.deleteById(id); // Eliminamos la mascota
        
        if (esAdmin) {
            return "redirect:/admin"; // Si es admin, redirige al panel admin
        }
        return "redirect:/dashboard"; // Si es dueño, redirige al dashboard
    }

    // --- MÉTODOS DE EXPORTACIÓN --- 

    // Método para exportar las mascotas a un archivo CSV
    @GetMapping(value = "/export/csv", produces = "text/csv")
    public void exportMascotasCSV(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv"); // Establecemos el tipo de contenido a CSV
        response.setHeader("Content-Disposition", "attachment; filename=mascotas.csv"); // Definimos el nombre del archivo

        PrintWriter writer = response.getWriter();
        writer.println("id,nombre,especie,raza,fecha_nacimiento,peso,foto_url,dueno_id"); // Escribimos los encabezados del CSV

        List<Mascota> mascotas = mascotaRepository.findAll(); // Obtenemos todas las mascotas

        // Iteramos sobre las mascotas y las escribimos en el CSV
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

        writer.flush(); // Enviamos los datos al cliente
        writer.close(); // Cerramos el escritor
    }

    // Método para exportar las mascotas a un archivo PDF
    @GetMapping(value = "/export/pdf", produces = "application/pdf")
    public void exportMascotasPDF(HttpServletResponse response) throws Exception {
        response.setContentType("application/pdf"); // Establecemos el tipo de contenido a PDF
        response.setHeader("Content-Disposition", "attachment; filename=mascotas.pdf"); // Definimos el nombre del archivo

        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream()); // Inicializamos el escritor del PDF

        document.open(); // Abrimos el documento PDF
        document.add(new Paragraph("Lista de Mascotas")); // Agregamos un título al PDF
        document.add(new Paragraph(" ")); // Añadimos un espacio en blanco

        List<Mascota> mascotas = mascotaRepository.findAll(); // Obtenemos todas las mascotas

        PdfPTable table = new PdfPTable(8); // Creamos una tabla con 8 columnas
        table.setWidthPercentage(100); // Establecemos el ancho de la tabla al 100%

        // Agregamos los encabezados a la tabla
        table.addCell("ID");
        table.addCell("Nombre");
        table.addCell("Especie");
        table.addCell("Raza");
        table.addCell("Nacimiento");
        table.addCell("Peso");
        table.addCell("Foto URL");
        table.addCell("Dueño ID");

        // Iteramos sobre las mascotas y agregamos los datos a la tabla
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

        document.add(table); // Agregamos la tabla al documento
        document.close(); // Cerramos el documento PDF
    }
}
