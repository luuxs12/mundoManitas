package com.mundomanitas.dto.view;

import java.util.List;

/**
 * Resumen de las reseñas de un curso: promedio, total y la opinión del
 * estudiante actual, si ya escribió una.
 */
public class ResenaResumenView {

    private final List<ResenaView> resenas;
    private final double promedio;
    private final long total;
    private final boolean puedeResenar;
    private final ResenaView miResena;

    public ResenaResumenView(
        List<ResenaView> resenas,
        double promedio,
        long total,
        boolean puedeResenar,
        ResenaView miResena
    ) {
        this.resenas = resenas;
        this.promedio = promedio;
        this.total = total;
        this.puedeResenar = puedeResenar;
        this.miResena = miResena;
    }

    public List<ResenaView> getResenas() {
        return resenas;
    }

    public double getPromedio() {
        return promedio;
    }

    public long getTotal() {
        return total;
    }

    public boolean isPuedeResenar() {
        return puedeResenar;
    }

    public ResenaView getMiResena() {
        return miResena;
    }

    public String getPromedioTexto() {
        return String.format(java.util.Locale.ROOT, "%.1f", promedio);
    }
}
