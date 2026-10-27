package com.mundomanitas.controller;

import com.mundomanitas.service.CertificadoService;
import com.mundomanitas.service.ReporteService;
import com.mundomanitas.service.UsuarioActualService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.charset.StandardCharsets;

@Controller
public class ReporteController {

    private static final MediaType EXCEL = MediaType.parseMediaType(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private static final MediaType WORD = MediaType.parseMediaType(
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private final ReporteService reporteService;
    private final CertificadoService certificadoService;
    private final UsuarioActualService usuarioActual;

    public ReporteController(
        ReporteService reporteService,
        CertificadoService certificadoService,
        UsuarioActualService usuarioActual
    ) {
        this.reporteService = reporteService;
        this.certificadoService = certificadoService;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping("/reportes/progreso.xlsx")
    public ResponseEntity<byte[]> descargarReporte() {
        byte[] archivo = reporteService.generarReporteProgreso();

        return ResponseEntity.ok()
            .contentType(EXCEL)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                    .filename("reporte-progreso.xlsx", StandardCharsets.UTF_8)
                    .build()
                    .toString()
            )
            .body(archivo);
    }

    @GetMapping("/reportes/certificado/{idModulo}.docx")
    public ResponseEntity<byte[]> descargarCertificado(
        @PathVariable Integer idModulo,
        Authentication authentication
    ) {
        Integer idEstudiante = usuarioActual.idEstudiante(authentication);
        byte[] archivo = certificadoService.generar(idEstudiante, idModulo);

        return ResponseEntity.ok()
            .contentType(WORD)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                    .filename("certificado.docx", StandardCharsets.UTF_8)
                    .build()
                    .toString()
            )
            .body(archivo);
    }
}
