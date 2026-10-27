package com.mundomanitas.dto.view;

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
        boolean inscrito
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
}
