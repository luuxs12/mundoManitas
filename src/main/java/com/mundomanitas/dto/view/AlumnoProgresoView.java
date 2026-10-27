package com.mundomanitas.dto.view;

import java.time.LocalDateTime;

/**
 * Fila del listado de alumnos inscritos en un curso, con su avance.
 */
public class AlumnoProgresoView {

    private final Integer idEstudiante;
    private final String nombre;
    private final String email;
    private final int porcentaje;
    private final long ejerciciosCorrectos;
    private final LocalDateTime fechaInicio;
    private final LocalDateTime fechaCompletado;

    public AlumnoProgresoView(
        Integer idEstudiante,
        String nombre,
        String email,
        int porcentaje,
        long ejerciciosCorrectos,
        LocalDateTime fechaInicio,
        LocalDateTime fechaCompletado
    ) {
        this.idEstudiante = idEstudiante;
        this.nombre = nombre;
        this.email = email;
        this.porcentaje = porcentaje;
        this.ejerciciosCorrectos = ejerciciosCorrectos;
        this.fechaInicio = fechaInicio;
        this.fechaCompletado = fechaCompletado;
    }

    public Integer getIdEstudiante() {
        return idEstudiante;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public int getPorcentaje() {
        return porcentaje;
    }

    public long getEjerciciosCorrectos() {
        return ejerciciosCorrectos;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public LocalDateTime getFechaCompletado() {
        return fechaCompletado;
    }

    public String getEstado() {
        if (porcentaje >= 100) {
            return "Completado";
        }
        if (porcentaje >= 50) {
            return "En progreso";
        }
        if (porcentaje > 0) {
            return "Iniciado";
        }
        return "Sin iniciar";
    }

    public String getEstadoClase() {
        if (porcentaje >= 100) {
            return "ok";
        }
        if (porcentaje >= 50) {
            return "info";
        }
        if (porcentaje > 0) {
            return "warn";
        }
        return "muted";
    }

    public String getInicial() {
        return nombre == null || nombre.isBlank()
            ? "?"
            : nombre.substring(0, 1).toUpperCase();
    }
}
