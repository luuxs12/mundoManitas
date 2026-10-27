package com.mundomanitas.dto.api;

public record ModuloDto(
    Integer idModulo,
    String titulo,
    String descripcion,
    String nivel,
    Integer orden
) {
    public Integer getIdModulo() {
        return idModulo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getNivel() {
        return nivel;
    }

    public Integer getOrden() {
        return orden;
    }
}
