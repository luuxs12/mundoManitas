package com.mundomanitas.dto.form;

/**
 * Cambio de contraseña desde el perfil: exige la contraseña actual
 * para confirmar la identidad del usuario.
 */
public class ContrasenaForm {

    private String actual;
    private String nueva;
    private String confirmar;

    public String getActual() {
        return actual;
    }

    public void setActual(String actual) {
        this.actual = actual;
    }

    public String getNueva() {
        return nueva;
    }

    public void setNueva(String nueva) {
        this.nueva = nueva;
    }

    public String getConfirmar() {
        return confirmar;
    }

    public void setConfirmar(String confirmar) {
        this.confirmar = confirmar;
    }
}
