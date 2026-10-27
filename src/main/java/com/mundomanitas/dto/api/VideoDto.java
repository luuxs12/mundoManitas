package com.mundomanitas.dto.api;

public record VideoDto(
    Integer idVideo,
    Integer idModulo,
    String titulo,
    String url,
    Integer duracion,
    Integer orden
) {
    public Integer getIdVideo() {
        return idVideo;
    }

    public Integer getIdModulo() {
        return idModulo;
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

    public Integer getOrden() {
        return orden;
    }
}
