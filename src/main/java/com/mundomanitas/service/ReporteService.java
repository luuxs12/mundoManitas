package com.mundomanitas.service;

import com.mundomanitas.dao.CursoDao;
import com.mundomanitas.dao.ProgresoDao;
import com.mundomanitas.dao.UsuarioDao;
import com.mundomanitas.entity.Modulo;
import com.mundomanitas.entity.Progreso;
import com.mundomanitas.entity.Usuario;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Generación de reportes académicos en Excel con Apache POI.
 */
@Service
public class ReporteService {

    private static final DateTimeFormatter FORMATO_FECHA =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final UsuarioDao usuarioDao;
    private final ProgresoDao progresoDao;
    private final CursoDao cursoDao;

    public ReporteService(
        UsuarioDao usuarioDao,
        ProgresoDao progresoDao,
        CursoDao cursoDao
    ) {
        this.usuarioDao = usuarioDao;
        this.progresoDao = progresoDao;
        this.cursoDao = cursoDao;
    }

    /**
     * Reporte general de progreso por estudiante y módulo.
     */
    @Transactional(readOnly = true)
    public byte[] generarReporteProgreso() {
        List<Usuario> estudiantes = usuarioDao.listarConRol().stream()
            .filter(usuario -> "ESTUDIANTE".equalsIgnoreCase(usuario.getRol().getCodigo()))
            .toList();

        Map<Integer, Modulo> modulos = cursoDao.listarModulos().stream()
            .collect(Collectors.toMap(Modulo::getIdModulo, Function.identity()));

        try (Workbook libro = new XSSFWorkbook();
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {

            Sheet hoja = libro.createSheet("Progreso");

            CellStyle encabezado = estiloEncabezado(libro);
            CellStyle texto = estiloTexto(libro);

            String[] columnas = {
                "Estudiante",
                "Correo",
                "Módulo",
                "Nivel",
                "Porcentaje",
                "Estado",
                "Calificación",
                "Fecha de inicio"
            };

            Row filaTitulo = hoja.createRow(0);
            Cell celdaTitulo = filaTitulo.createCell(0);
            celdaTitulo.setCellValue(
                "Reporte de progreso - Mundo Manitas - "
                    + LocalDateTime.now().format(FORMATO_FECHA)
            );
            celdaTitulo.setCellStyle(encabezado);

            Row filaCabecera = hoja.createRow(2);
            for (int i = 0; i < columnas.length; i++) {
                Cell celda = filaCabecera.createCell(i);
                celda.setCellValue(columnas[i]);
                celda.setCellStyle(encabezado);
            }

            int numeroFila = 3;

            for (Usuario estudiante : estudiantes) {
                Map<Integer, Progreso> progresos = progresoDao
                    .listarPorEstudiante(estudiante.getIdUsuario())
                    .stream()
                    .collect(
                        Collectors.toMap(
                            Progreso::getIdModulo,
                            Function.identity(),
                            (primero, segundo) -> primero
                        )
                    );

                for (Modulo modulo : modulos.values()) {
                    Progreso progreso = progresos.get(modulo.getIdModulo());
                    int porcentaje = porcentaje(progreso);

                    Row fila = hoja.createRow(numeroFila++);

                    crearCelda(fila, 0, estudiante.getNombre(), texto);
                    crearCelda(fila, 1, estudiante.getEmail(), texto);
                    crearCelda(fila, 2, modulo.getTitulo(), texto);
                    crearCelda(fila, 3, modulo.getNivel(), texto);
                    crearCelda(fila, 4, porcentaje + " %", texto);
                    crearCelda(fila, 5, estado(porcentaje), texto);
                    crearCelda(
                        fila,
                        6,
                        progreso == null || progreso.getCalificacion() == null
                            ? "-"
                            : progreso.getCalificacion().toPlainString(),
                        texto
                    );
                    crearCelda(
                        fila,
                        7,
                        progreso == null || progreso.getFechaInicio() == null
                            ? "-"
                            : progreso.getFechaInicio().format(FORMATO_FECHA),
                        texto
                    );
                }
            }

            for (int i = 0; i < columnas.length; i++) {
                hoja.autoSizeColumn(i);
            }

            libro.write(salida);

            return salida.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No fue posible generar el reporte Excel.", ex);
        }
    }

    private CellStyle estiloEncabezado(Workbook libro) {
        CellStyle estilo = libro.createCellStyle();
        Font fuente = libro.createFont();

        fuente.setBold(true);
        fuente.setColor(IndexedColors.WHITE.getIndex());

        estilo.setFont(fuente);
        estilo.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        estilo.setBorderBottom(BorderStyle.THIN);

        return estilo;
    }

    private CellStyle estiloTexto(Workbook libro) {
        CellStyle estilo = libro.createCellStyle();
        estilo.setBorderBottom(BorderStyle.HAIR);
        return estilo;
    }

    private void crearCelda(Row fila, int indice, String valor, CellStyle estilo) {
        Cell celda = fila.createCell(indice);
        celda.setCellValue(valor);
        celda.setCellStyle(estilo);
    }

    private int porcentaje(Progreso progreso) {
        return progreso == null || progreso.getPorcentaje() == null
            ? 0
            : progreso.getPorcentaje().intValue();
    }

    private String estado(int porcentaje) {
        if (porcentaje >= 100) {
            return "COMPLETADO";
        }
        if (porcentaje > 0) {
            return "EN PROGRESO";
        }
        return "POR INICIAR";
    }
}
