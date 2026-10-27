package com.mundomanitas.dto.view;

/**
 * Resumen de un curso para el panel del docente: cuántos alumnos tiene,
 * cómo van y cuánto contenido publicado existe.
 */
public class CursoDocenteView {

    private final Integer idModulo;
    private final String titulo;
    private final String nivel;
    private final Integer orden;
    private final long inscritos;
    private final int promedio;
    private final long completados;
    private final int videos;
    private final int ejercicios;

    public CursoDocenteView(
        Integer idModulo,
        String titulo,
        String nivel,
        Integer orden,
        long inscritos,
        int promedio,
        long completados,
        int videos,
        int ejercicios
    ) {
        this.idModulo = idModulo;
        this.titulo = titulo;
        this.nivel = nivel;
        this.orden = orden;
        this.inscritos = inscritos;
        this.promedio = promedio;
        this.completados = completados;
        this.videos = videos;
        this.ejercicios = ejercicios;
    }

    public Integer getIdModulo() {
        return idModulo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getNivel() {
        return nivel;
    }

    public Integer getOrden() {
        return orden;
    }

    public long getInscritos() {
        return inscritos;
    }

    public int getPromedio() {
        return promedio;
    }

    public long getCompletados() {
        return completados;
    }

    public int getVideos() {
        return videos;
    }

    public int getEjercicios() {
        return ejercicios;
    }

    public String getEstado() {
        if (promedio >= 80) {
            return "Excelente";
        }
        if (promedio >= 50) {
            return "En ritmo";
        }
        if (promedio > 0) {
            return "Requiere apoyo";
        }
        return "Sin actividad";
    }

    public String getEstadoClase() {
        if (promedio >= 80) {
            return "ok";
        }
        if (promedio >= 50) {
            return "info";
        }
        if (promedio > 0) {
            return "warn";
        }
        return "muted";
    }
}
