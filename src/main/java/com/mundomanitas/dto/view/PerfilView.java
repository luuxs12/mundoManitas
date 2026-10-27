package com.mundomanitas.dto.view;

public class PerfilView {

    private final String nombre;
    private final String correo;
    private final String tipo;
    private final boolean esEstudiante;
    private final EstudianteView estudiante;
    private final int totalModulos;
    private final long completados;
    private final int progresoGeneral;

    public PerfilView(
        String nombre,
        String correo,
        String tipo,
        boolean esEstudiante,
        EstudianteView estudiante,
        int totalModulos,
        long completados,
        int progresoGeneral
    ) {
        this.nombre = nombre;
        this.correo = correo;
        this.tipo = tipo;
        this.esEstudiante = esEstudiante;
        this.estudiante = estudiante;
        this.totalModulos = totalModulos;
        this.completados = completados;
        this.progresoGeneral = progresoGeneral;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getTipo() {
        return tipo;
    }

    public boolean isEsEstudiante() {
        return esEstudiante;
    }

    public EstudianteView getEstudiante() {
        return estudiante;
    }

    public int getTotalModulos() {
        return totalModulos;
    }

    public long getCompletados() {
        return completados;
    }

    public int getProgresoGeneral() {
        return progresoGeneral;
    }
}
