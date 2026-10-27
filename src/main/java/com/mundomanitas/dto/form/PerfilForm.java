package com.mundomanitas.dto.form;

/**
 * Datos editables por el propio usuario desde su perfil.
 * El correo no se edita aquí porque identifica la sesión activa.
 */
public class PerfilForm {

    private String nombre;
    private String grado;
    private Integer edad;
    private String nivelEstudio;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getGrado() {
        return grado;
    }

    public void setGrado(String grado) {
        this.grado = grado;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public String getNivelEstudio() {
        return nivelEstudio;
    }

    public void setNivelEstudio(String nivelEstudio) {
        this.nivelEstudio = nivelEstudio;
    }
}
