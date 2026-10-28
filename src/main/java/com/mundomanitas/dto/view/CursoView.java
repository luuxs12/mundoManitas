package com.mundomanitas.dto.view;

import java.math.BigDecimal;

/**
 * DTO de vista para la tarjeta de un módulo en el listado de cursos.
 */
public class CursoView {

    private final Integer idModulo;
    private final String titulo;
    private final String descripcion;
    private final String nivel;
    private final Integer orden;
    private final int porcentaje;
    private final String estado;
    private final String estadoClase;
    private final String accion;
    private final String coverClase;
    private final String inicial;
    private final boolean inscrito;
    private final boolean premium;
    private final BigDecimal precio;
    private final boolean comprado;
    private final boolean accesible;
    private final boolean favorito;

    public CursoView(
        Integer idModulo,
        String titulo,
        String descripcion,
        String nivel,
        Integer orden,
        int porcentaje,
        String estado,
        String estadoClase,
        String accion,
        String coverClase,
        String inicial,
        boolean inscrito,
        boolean premium,
        BigDecimal precio,
        boolean comprado,
        boolean accesible,
        boolean favorito
    ) {
        this.idModulo = idModulo;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.nivel = nivel;
        this.orden = orden;
        this.porcentaje = porcentaje;
        this.estado = estado;
        this.estadoClase = estadoClase;
        this.accion = accion;
        this.coverClase = coverClase;
        this.inicial = inicial;
        this.inscrito = inscrito;
        this.premium = premium;
        this.precio = precio == null ? BigDecimal.ZERO : precio;
        this.comprado = comprado;
        this.accesible = accesible;
        this.favorito = favorito;
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

    public int getPorcentaje() {
        return porcentaje;
    }

    public String getEstado() {
        return estado;
    }

    public String getEstadoClase() {
        return estadoClase;
    }

    public String getAccion() {
        return accion;
    }

    public String getCoverClase() {
        return coverClase;
    }

    public String getInicial() {
        return inicial;
    }

    public boolean isInscrito() {
        return inscrito;
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

    public boolean isAccesible() {
        return accesible;
    }

    public boolean isFavorito() {
        return favorito;
    }
}
