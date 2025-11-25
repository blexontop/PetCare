package petcare.petcare.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import petcare.petcare.service.ExportMascotaService;

@RestController
@RequiredArgsConstructor
public class ExportMascotaController {

    private final ExportMascotaService exportMascotaService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/mascotas/export/csv")
    public ResponseEntity<byte[]> exportCSV() {
        byte[] csvBytes = exportMascotaService.exportToCSV();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=mascotas.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/mascotas/export/pdf")
    public ResponseEntity<byte[]> exportPDF() {
        byte[] pdfBytes = exportMascotaService.exportToPDF();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=mascotas.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
