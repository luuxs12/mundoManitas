package com.mundomanitas.dto.view;

public class EstudianteView {

    private final String grado;
    private final Integer edad;
    private final String nivelEstudio;

    public EstudianteView(String grado, Integer edad, String nivelEstudio) {
        this.grado = grado;
        this.edad = edad;
        this.nivelEstudio = nivelEstudio;
    }

    public String getGrado() {
        return grado;
    }

    public Integer getEdad() {
        return edad;
    }

    public String getNivelEstudio() {
        return nivelEstudio;
    }
}
