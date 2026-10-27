package com.mundomanitas.dto.view;

public class ModuloView {

    private final Integer idModulo;
    private final String titulo;
    private final String descripcion;
    private final String nivel;
    private final Integer orden;

    public ModuloView(
        Integer idModulo,
        String titulo,
        String descripcion,
        String nivel,
        Integer orden
    ) {
        this.idModulo = idModulo;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.nivel = nivel;
        this.orden = orden;
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
}
