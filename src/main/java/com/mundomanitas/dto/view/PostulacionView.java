package com.mundomanitas.dto.view;

import com.mundomanitas.entity.EstadoPostulacion;

import java.time.format.DateTimeFormatter;

/**
 * Vista de una postulación docente para el panel de administración.
 *
 * <p>Expone getters JavaBean para que Thymeleaf pueda resolver las
 * propiedades sin ambigüedad y muestra fechas ya formateadas.</p>
 */
public class PostulacionView {

    private static final DateTimeFormatter FORMATO =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Integer id;
    private final String nombre;
    private final String email;
    private final String telefono;
    private final String nivelLsp;
    private final String certificacionLsp;
    private final Integer aniosExperiencia;
    private final String areaTi;
    private final String portafolio;
    private final String motivacion;
    private final EstadoPostulacion estado;
    private final String estadoEtiqueta;
    private final String observacion;
    private final String fechaPostulacion;
    private final String fechaResolucion;
    private final Integer idUsuario;
    private final String moduloAsignado;

    public PostulacionView(
        Integer id,
        String nombre,
        String email,
        String telefono,
        String nivelLsp,
        String certificacionLsp,
        Integer aniosExperiencia,
        String areaTi,
        String portafolio,
        String motivacion,
        EstadoPostulacion estado,
        String observacion,
        java.time.LocalDateTime fechaPostulacion,
        java.time.LocalDateTime fechaResolucion,
        Integer idUsuario,
        String moduloAsignado
    ) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.nivelLsp = nivelLsp;
        this.certificacionLsp = certificacionLsp;
        this.aniosExperiencia = aniosExperiencia;
        this.areaTi = areaTi;
        this.portafolio = portafolio;
        this.motivacion = motivacion;
        this.estado = estado;
        this.estadoEtiqueta = estado == null ? "" : estado.getEtiqueta();
        this.observacion = observacion;
        this.fechaPostulacion = fechaPostulacion == null ? "" : fechaPostulacion.format(FORMATO);
        this.fechaResolucion = fechaResolucion == null ? "" : fechaResolucion.format(FORMATO);
        this.idUsuario = idUsuario;
        this.moduloAsignado = moduloAsignado;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getNivelLsp() {
        return nivelLsp;
    }

    public String getCertificacionLsp() {
        return certificacionLsp;
    }

    public Integer getAniosExperiencia() {
        return aniosExperiencia;
    }

    public String getAreaTi() {
        return areaTi;
    }

    public String getPortafolio() {
        return portafolio;
    }

    public String getMotivacion() {
        return motivacion;
    }

    public EstadoPostulacion getEstado() {
        return estado;
    }

    public String getEstadoEtiqueta() {
        return estadoEtiqueta;
    }

    public String getObservacion() {
        return observacion;
    }

    public String getFechaPostulacion() {
        return fechaPostulacion;
    }

    public String getFechaResolucion() {
        return fechaResolucion;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public String getModuloAsignado() {
        return moduloAsignado;
    }

    public boolean isPendiente() {
        return estado != null && estado.estaPendiente();
    }

    public boolean isAprobada() {
        return estado == EstadoPostulacion.APROBADA;
    }

    public boolean isRechazada() {
        return estado == EstadoPostulacion.RECHAZADA;
    }
}
