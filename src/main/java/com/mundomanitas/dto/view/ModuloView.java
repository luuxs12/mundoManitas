package com.mundomanitas.dto.view;

import java.math.BigDecimal;

public class ModuloView {

    private final Integer idModulo;
    private final String titulo;
    private final String descripcion;
    private final String nivel;
    private final Integer orden;
    private final boolean premium;
    private final BigDecimal precio;

    public ModuloView(
        Integer idModulo,
        String titulo,
        String descripcion,
        String nivel,
        Integer orden,
        boolean premium,
        BigDecimal precio
    ) {
        this.idModulo = idModulo;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.nivel = nivel;
        this.orden = orden;
        this.premium = premium;
        this.precio = precio == null ? BigDecimal.ZERO : precio;
    }

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
