package com.mundomanitas.dto.view;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Reseña lista para mostrar en la ficha del curso.
 */
public class ResenaView {

    private static final DateTimeFormatter FECHA =
        DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Integer idResena;
    private final Integer idEstudiante;
    private final String nombreEstudiante;
    private final String inicial;
    private final int calificacion;
    private final String comentario;
    private final LocalDateTime fecha;
    private final boolean propia;

    public ResenaView(
        Integer idResena,
        Integer idEstudiante,
        String nombreEstudiante,
        String inicial,
        int calificacion,
        String comentario,
        LocalDateTime fecha,
        boolean propia
    ) {
        this.idResena = idResena;
        this.idEstudiante = idEstudiante;
        this.nombreEstudiante = nombreEstudiante;
        this.inicial = inicial;
        this.calificacion = calificacion;
        this.comentario = comentario;
        this.fecha = fecha;
        this.propia = propia;
    }

    public Integer getIdResena() {
        return idResena;
    }

    public Integer getIdEstudiante() {
        return idEstudiante;
    }

    public String getNombreEstudiante() {
        return nombreEstudiante;
    }

    public String getInicial() {
        return inicial;
    }

    public int getCalificacion() {
        return calificacion;
    }

    public String getComentario() {
        return comentario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getFechaTexto() {
        return fecha == null ? "" : fecha.format(FECHA);
    }

    public boolean isPropia() {
        return propia;
    }
}
