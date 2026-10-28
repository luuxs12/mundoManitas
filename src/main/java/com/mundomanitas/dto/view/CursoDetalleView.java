package com.mundomanitas.dto.view;

import java.math.BigDecimal;
import java.util.List;

public class CursoDetalleView {

    private final ModuloView modulo;
    private final List<VideoView> videos;
    private final List<EjercicioView> ejercicios;
    private final int porcentaje;
    private final String estado;
    private final String estadoClase;
    private final BigDecimal calificacion;
    private final boolean esEstudiante;
    private final boolean inscrito;
    private final int totalContenidos;
    private final int totalVideos;
    private final int totalEjercicios;
    private final int totalMinutos;
    private final VideoView siguienteVideo;
    private final boolean premium;
    private final BigDecimal precio;
    private final boolean comprado;
    private final boolean bloqueado;

    public CursoDetalleView(
        ModuloView modulo,
        List<VideoView> videos,
        List<EjercicioView> ejercicios,
        int porcentaje,
        String estado,
        String estadoClase,
        BigDecimal calificacion,
        boolean esEstudiante,
        boolean inscrito,
        int totalContenidos,
        int totalVideos,
        int totalEjercicios,
        int totalMinutos,
        VideoView siguienteVideo,
        boolean premium,
        BigDecimal precio,
        boolean comprado,
        boolean bloqueado
    ) {
        this.modulo = modulo;
        this.videos = videos;
        this.ejercicios = ejercicios;
        this.porcentaje = porcentaje;
        this.estado = estado;
        this.estadoClase = estadoClase;
        this.calificacion = calificacion;
        this.esEstudiante = esEstudiante;
        this.inscrito = inscrito;
        this.totalContenidos = totalContenidos;
        this.totalVideos = totalVideos;
        this.totalEjercicios = totalEjercicios;
        this.totalMinutos = totalMinutos;
        this.siguienteVideo = siguienteVideo;
        this.premium = premium;
        this.precio = precio == null ? BigDecimal.ZERO : precio;
        this.comprado = comprado;
        this.bloqueado = bloqueado;
    }

    public ModuloView getModulo() {
        return modulo;
    }

    public List<VideoView> getVideos() {
        return videos;
    }

    public List<EjercicioView> getEjercicios() {
        return ejercicios;
    }

    public int getPorcentaje() {
        return porcentaje;
    }

    public String getEstado() {
        return estado;
    }

    public String getEstadoClase() {
        return estadoClase;
    }

    public BigDecimal getCalificacion() {
        return calificacion;
    }

    public boolean isEsEstudiante() {
        return esEstudiante;
    }

    public boolean isInscrito() {
        return inscrito;
    }

    public int getTotalContenidos() {
        return totalContenidos;
    }

    public int getTotalVideos() {
        return totalVideos;
    }

    public int getTotalEjercicios() {
        return totalEjercicios;
    }

    public int getTotalMinutos() {
        return totalMinutos;
    }

    public VideoView getSiguienteVideo() {
        return siguienteVideo;
    }

    public boolean isPremium() {
        return premium;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public boolean isComprado() {
        return comprado;
    }

    public boolean isBloqueado() {
        return bloqueado;
    }
}
