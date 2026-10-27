package com.mundomanitas.dto.view;

/**
 * Tarjeta de indicador (KPI) para los paneles.
 *
 * <p>El tono controla el color de acento en la interfaz: {@code ok},
 * {@code info}, {@code warn}, {@code danger} o {@code neutral}.</p>
 */
public class KpiView {

    private final String etiqueta;
    private final String valor;
    private final String detalle;
    private final String tono;
    private final String icono;

    public KpiView(String etiqueta, String valor, String detalle, String tono, String icono) {
        this.etiqueta = etiqueta;
        this.valor = valor;
        this.detalle = detalle;
        this.tono = tono;
        this.icono = icono;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getValor() {
        return valor;
    }

    public String getDetalle() {
        return detalle;
    }

    public String getTono() {
        return tono;
    }

    public String getIcono() {
        return icono;
    }
}
