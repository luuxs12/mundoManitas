package com.mundomanitas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Solicitud de alta como docente.
 *
 * <p>No es una cuenta: es la evidencia que el aspirante presenta para que
 * el administrador valide su dominio de Lengua de Señas Peruana (LSP) y su
 * experiencia en TI. Solo una postulación aprobada se convierte en un
 * {@link Usuario} con rol DOCENTE y en la asignación de un {@link Modulo}.</p>
 */
@Entity
@Table(name = "postulacion_docente")
public class PostulacionDocente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPostulacion")
    private Integer idPostulacion;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "telefono", nullable = false, length = 20)
    private String telefono;

    /**
     * Nivel acreditado del Marco Común Europeo de Referencia aplicado a la
     * LSP. Solo se aceptan niveles B1 o superiores.
     */
    @Column(name = "nivelLsp", nullable = false, length = 10)
    private String nivelLsp;

    @Column(name = "certificacionLsp", nullable = false, length = 200)
    private String certificacionLsp;

    @Column(name = "aniosExperiencia", nullable = false)
    private Integer aniosExperiencia;

    @Column(name = "areaTi", nullable = false, length = 120)
    private String areaTi;

    @Column(name = "portafolio", nullable = false, length = 300)
    private String portafolio;

    @Column(name = "motivacion", nullable = false, columnDefinition = "TEXT")
    private String motivacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPostulacion estado = EstadoPostulacion.PENDIENTE;

    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;

    @Column(name = "fechaPostulacion", nullable = false)
    private LocalDateTime fechaPostulacion;

    @Column(name = "fechaResolucion")
    private LocalDateTime fechaResolucion;

    /**
     * Cuenta DOCENTE creada al aprobar la postulación.
     */
    @Column(name = "idUsuario")
    private Integer idUsuario;

    /**
     * Módulo educativo asignado al docente aprobado.
     */
    @Column(name = "idModulo")
    private Integer idModulo;

    public Integer getIdPostulacion() {
        return idPostulacion;
    }

    public void setIdPostulacion(Integer idPostulacion) {
        this.idPostulacion = idPostulacion;
    }

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

    public EstadoPostulacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoPostulacion estado) {
        this.estado = estado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public LocalDateTime getFechaPostulacion() {
        return fechaPostulacion;
    }

    public void setFechaPostulacion(LocalDateTime fechaPostulacion) {
        this.fechaPostulacion = fechaPostulacion;
    }

    public LocalDateTime getFechaResolucion() {
        return fechaResolucion;
    }

    public void setFechaResolucion(LocalDateTime fechaResolucion) {
        this.fechaResolucion = fechaResolucion;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdModulo() {
        return idModulo;
    }

    public void setIdModulo(Integer idModulo) {
        this.idModulo = idModulo;
    }

    public boolean isPendiente() {
        return estado != null && estado.estaPendiente();
    }
}
