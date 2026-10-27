package com.mundomanitas.dto.view;

import java.util.List;

public class DashboardEstudianteView {

    private final int totalCursos;
    private final long enProgreso;
    private final long completados;
    private final long porIniciar;
    private final int progresoGeneral;
    private final List<CursoResumenView> cursosActivos;

    public DashboardEstudianteView(
        int totalCursos,
        long enProgreso,
        long completados,
        long porIniciar,
        int progresoGeneral,
        List<CursoResumenView> cursosActivos
    ) {
        this.totalCursos = totalCursos;
        this.enProgreso = enProgreso;
        this.completados = completados;
        this.porIniciar = porIniciar;
        this.progresoGeneral = progresoGeneral;
        this.cursosActivos = cursosActivos;
    }

    public int getTotalCursos() {
        return totalCursos;
    }

    public long getEnProgreso() {
        return enProgreso;
    }

    public long getCompletados() {
        return completados;
    }

    public long getPorIniciar() {
        return porIniciar;
    }

    public int getProgresoGeneral() {
        return progresoGeneral;
    }

    public List<CursoResumenView> getCursosActivos() {
        return cursosActivos;
    }
}
