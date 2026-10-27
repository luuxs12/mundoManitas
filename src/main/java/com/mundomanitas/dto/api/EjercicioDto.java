package com.mundomanitas.dto.api;

public record EjercicioDto(
    Integer idEjercicio,
    Integer idModulo,
    String titulo,
    String tipo,
    String contenido
) {
    public Integer getIdEjercicio() {
        return idEjercicio;
    }

    public Integer getIdModulo() {
        return idModulo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getTipo() {
        return tipo;
    }

    public String getContenido() {
        return contenido;
    }
}
