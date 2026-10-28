package com.mundomanitas.dto.api;

import java.math.BigDecimal;

public record ModuloDto(
    Integer idModulo,
    String titulo,
    String descripcion,
    String nivel,
    Integer orden,
    boolean premium,
    BigDecimal precio
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

    public boolean isPremium() {
        return premium;
    }

    public BigDecimal getPrecio() {
        return precio;
    }
}
