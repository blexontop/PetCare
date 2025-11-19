package petcare.petcare.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
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
import petcare.petcare.repository.DuenoRepository;
import petcare.petcare.repository.MascotaRepository;
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


    @GetMapping("/mascotas")
    public String listarMascotas(Model model) {
        List<Mascota> mascotas = mascotaRepository.findAll();
        model.addAttribute("mascotas", mascotas);
        return "mascotas";
    }

    @GetMapping("/mascotas/nueva")
    public String mostrarFormularioNuevaMascota(Model model) {
        model.addAttribute("duenos", duenoRepository.findAll());
        return "mascota-form";
    }

    @PostMapping("/mascotas")
    public String guardarMascota(@RequestParam String nombre,
                                 @RequestParam String especie,
                                 @RequestParam(required = false) String raza,
                                 @RequestParam(required = false) String fechaNacimiento,
                                 @RequestParam(required = false) Double peso,
                                 @RequestParam Long duenoId) {

        Dueno dueno = duenoRepository.findById(duenoId).orElse(null);

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

        // Integrar imagen de Dog/Cat API basada en especie
        if ("PERRO".equalsIgnoreCase(especie)) {
            mascota.setFotoUrl(dogCatApiService.obtenerImagenPerroAleatoria());
        } else if ("GATO".equalsIgnoreCase(especie)) {
            mascota.setFotoUrl(dogCatApiService.obtenerImagenGatoAleatoria());
        }
        // Para otras especies, fotoUrl queda null

        mascotaRepository.save(mascota);
        return "redirect:/mascotas";
    }

    @GetMapping("/mascotas/{id}/eliminar")
    public String eliminarMascota(@PathVariable Long id) {
        mascotaRepository.deleteById(id);
        return "redirect:/mascotas";
    }

    //Metodo para exportar mascotas a CSV
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
            (m.getDueno() != null ? m.getDueno().getId() : "")
        );
    }

    writer.flush();
    writer.close();
    }

    //Metodo para exportar mascotas a PDF
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
