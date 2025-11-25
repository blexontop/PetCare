package petcare.petcare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import petcare.petcare.model.Mascota;
import petcare.petcare.repository.MascotaRepository;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportMascotaService {

    private final MascotaRepository mascotaRepository;

    // ---- EXPORTACIÓN CSV ----
    public byte[] exportToCSV() {

        List<Mascota> mascotas = mascotaRepository.findAll();

        StringBuilder sb = new StringBuilder();
        sb.append("ID,Nombre,Especie,Raza,FechaNacimiento,Peso,Dueño\n");

        for (Mascota m : mascotas) {
            sb.append(m.getId()).append(",");
            sb.append(m.getNombre()).append(",");
            sb.append(m.getEspecie()).append(",");
            sb.append(m.getRaza() != null ? m.getRaza() : "").append(",");
            sb.append(m.getFechaNacimiento() != null ? m.getFechaNacimiento() : "").append(",");
            sb.append(m.getPeso() != null ? m.getPeso() : "").append(",");
            sb.append(m.getDueno() != null ? m.getDueno().getNombre() : "").append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportToPDF() {
        List<Mascota> mascotas = mascotaRepository.findAll();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            document.add(new Paragraph("Listado de Mascotas"));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(6);
            table.addCell("ID");
            table.addCell("Nombre");
            table.addCell("Especie");
            table.addCell("Raza");
            table.addCell("Peso");
            table.addCell("Dueño");

            for (Mascota m : mascotas) {
                table.addCell(String.valueOf(m.getId()));
                table.addCell(m.getNombre());
                table.addCell(m.getEspecie());
                table.addCell(m.getRaza() != null ? m.getRaza() : "");
                table.addCell(m.getPeso() != null ? m.getPeso().toString() : "");
                table.addCell(m.getDueno() != null ? m.getDueno().getNombre() : "");
            }

            document.add(table);
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }
}