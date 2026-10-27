package com.mundomanitas.dto.form;

/**
 * Datos del formulario público de postulación docente.
 *
 * <p>Este formulario no crea cuentas: recoge la evidencia profesional que
 * el administrador revisará. Por eso todos los campos son obligatorios y
 * la validación es estricta.</p>
 */
public class PostulacionDocenteForm {

    private String nombre;
    private String email;
    private String telefono;
    private String nivelLsp;
    private String certificacionLsp;
    private Integer aniosExperiencia;
    private String areaTi;
    private String portafolio;
    private String motivacion;
    private boolean aceptaTerminos;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getNivelLsp() {
        return nivelLsp;
    }

    public void setNivelLsp(String nivelLsp) {
        this.nivelLsp = nivelLsp;
    }

    public String getCertificacionLsp() {
        return certificacionLsp;
    }

    public void setCertificacionLsp(String certificacionLsp) {
        this.certificacionLsp = certificacionLsp;
    }

    public Integer getAniosExperiencia() {
        return aniosExperiencia;
    }

    public void setAniosExperiencia(Integer aniosExperiencia) {
        this.aniosExperiencia = aniosExperiencia;
    }

    public String getAreaTi() {
        return areaTi;
    }

    public void setAreaTi(String areaTi) {
        this.areaTi = areaTi;
    }

    public String getPortafolio() {
        return portafolio;
    }

    public void setPortafolio(String portafolio) {
        this.portafolio = portafolio;
    }

    public String getMotivacion() {
        return motivacion;
    }

    public void setMotivacion(String motivacion) {
        this.motivacion = motivacion;
    }

    public boolean isAceptaTerminos() {
        return aceptaTerminos;
    }

    public void setAceptaTerminos(boolean aceptaTerminos) {
        this.aceptaTerminos = aceptaTerminos;
    }
}
