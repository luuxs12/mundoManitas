package com.mundomanitas.dto.view;

public class VideoView {

    private final Integer idVideo;
    private final String titulo;
    private final String url;
    private final Integer duracion;
    private final int duracionMinutos;
    private final boolean completado;

    public VideoView(
        Integer idVideo,
        String titulo,
        String url,
        Integer duracion,
        int duracionMinutos,
        boolean completado
    ) {
        this.idVideo = idVideo;
        this.titulo = titulo;
        this.url = url;
        this.duracion = duracion;
        this.duracionMinutos = duracionMinutos;
        this.completado = completado;
    }

    public Integer getIdVideo() {
        return idVideo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getUrl() {
        return url;
    }

    public Integer getDuracion() {
        return duracion;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public boolean isCompletado() {
        return completado;
    }
}
