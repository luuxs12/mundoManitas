package com.mundomanitas.dto.view;

public class CursoResumenView {

    private final Integer idModulo;
    private final String titulo;
    private final String nivel;
    private final int porcentaje;

    public CursoResumenView(Integer idModulo, String titulo, String nivel, int porcentaje) {
        this.idModulo = idModulo;
        this.titulo = titulo;
        this.nivel = nivel;
        this.porcentaje = porcentaje;
    }

    public Integer getIdModulo() {
        return idModulo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getNivel() {
        return nivel;
    }

    public int getPorcentaje() {
        return porcentaje;
    }
}
