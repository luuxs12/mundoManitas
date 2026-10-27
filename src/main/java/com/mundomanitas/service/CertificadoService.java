package com.mundomanitas.service;

import com.mundomanitas.dao.CursoDao;
import com.mundomanitas.dao.ProgresoDao;
import com.mundomanitas.dao.UsuarioDao;
import com.mundomanitas.entity.Modulo;
import com.mundomanitas.entity.Progreso;
import com.mundomanitas.entity.Usuario;
import com.mundomanitas.exception.RecursoNoEncontradoException;
import com.mundomanitas.exception.ReglaNegocioException;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Generación de certificados en Word (.docx) con Apache POI.
 * Solo se emite cuando el estudiante completó el 100 % del módulo.
 */
@Service
public class CertificadoService {

    private static final DateTimeFormatter FORMATO_FECHA =
        DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy");

    private final UsuarioDao usuarioDao;
    private final ProgresoDao progresoDao;
    private final CursoDao cursoDao;

    public CertificadoService(
        UsuarioDao usuarioDao,
        ProgresoDao progresoDao,
        CursoDao cursoDao
    ) {
        this.usuarioDao = usuarioDao;
        this.progresoDao = progresoDao;
        this.cursoDao = cursoDao;
    }

    @Transactional(readOnly = true)
    public byte[] generar(Integer idEstudiante, Integer idModulo) {
        Usuario estudiante = usuarioDao.buscarPorIdConRol(idEstudiante)
            .orElseThrow(() -> new RecursoNoEncontradoException("Estudiante no encontrado"));

        Modulo modulo = cursoDao.buscarModulo(idModulo)
            .orElseThrow(() -> new RecursoNoEncontradoException("Módulo no encontrado"));

        Progreso progreso = progresoDao.buscar(idEstudiante, idModulo)
            .orElseThrow(() -> new ReglaNegocioException(
                "El estudiante aún no tiene progreso registrado en este módulo."
            ));

        int porcentaje = progreso.getPorcentaje() == null
            ? 0
            : progreso.getPorcentaje().intValue();

        if (porcentaje < 100) {
            throw new ReglaNegocioException(
                "El certificado solo se emite al completar el 100 % del módulo."
            );
        }

        try (XWPFDocument documento = new XWPFDocument();
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {

            titulo(documento, "Mundo Manitas");
            subtitulo(documento, "Plataforma educativa inclusiva");

            parrafo(documento, "", 12, false, ParagraphAlignment.CENTER);
            titulo(documento, "CERTIFICADO DE FINALIZACIÓN");

            parrafo(
                documento,
                "Se certifica que",
                14,
                false,
                ParagraphAlignment.CENTER
            );

            XWPFParagraph nombre = documento.createParagraph();
            nombre.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun runNombre = nombre.createRun();
            runNombre.setText(estudiante.getNombre());
            runNombre.setBold(true);
            runNombre.setFontSize(24);

            parrafo(
                documento,
                "ha completado satisfactoriamente el módulo",
                14,
                false,
                ParagraphAlignment.CENTER
            );

            XWPFParagraph moduloParrafo = documento.createParagraph();
            moduloParrafo.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun runModulo = moduloParrafo.createRun();
            runModulo.setText(modulo.getTitulo());
            runModulo.setBold(true);
            runModulo.setFontSize(18);

            parrafo(
                documento,
                "Nivel " + modulo.getNivel() + " - 100 % de avance",
                12,
                false,
                ParagraphAlignment.CENTER
            );

            if (progreso.getCalificacion() != null) {
                parrafo(
                    documento,
                    "Calificación obtenida: "
                        + progreso.getCalificacion().toPlainString(),
                    12,
                    false,
                    ParagraphAlignment.CENTER
                );
            }

            parrafo(documento, "", 12, false, ParagraphAlignment.CENTER);
            parrafo(
                documento,
                "Expedido el " + LocalDate.now().format(FORMATO_FECHA),
                12,
                false,
                ParagraphAlignment.CENTER
            );

            parrafo(documento, "", 12, false, ParagraphAlignment.CENTER);
            parrafo(
                documento,
                "Este certificado acredita el esfuerzo y la constancia del estudiante "
                    + "en su proceso de aprendizaje.",
                11,
                true,
                ParagraphAlignment.CENTER
            );

            documento.write(salida);

            return salida.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException(
                "No fue posible generar el certificado Word.",
                ex
            );
        }
    }

    private void titulo(XWPFDocument documento, String texto) {
        XWPFParagraph parrafo = documento.createParagraph();
        parrafo.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun run = parrafo.createRun();
        run.setText(texto);
        run.setBold(true);
        run.setFontSize(28);
    }

    private void subtitulo(XWPFDocument documento, String texto) {
        XWPFParagraph parrafo = documento.createParagraph();
        parrafo.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun run = parrafo.createRun();
        run.setText(texto);
        run.setItalic(true);
        run.setFontSize(12);
    }

    private void parrafo(
        XWPFDocument documento,
        String texto,
        int tamano,
        boolean cursiva,
        ParagraphAlignment alineacion
    ) {
        XWPFParagraph parrafo = documento.createParagraph();
        parrafo.setAlignment(alineacion);

        XWPFRun run = parrafo.createRun();
        run.setText(texto);
        run.setFontSize(tamano);
        run.setItalic(cursiva);
    }
}
