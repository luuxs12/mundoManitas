package com.mundomanitas.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Registro de cada intento de resolución de un ejercicio.
 */
@Entity
@Table(name = "intento_ejercicio")
public class IntentoEjercicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idIntento")
    private Integer idIntento;

    @Column(name = "idEstudiante", nullable = false)
    private Integer idEstudiante;

    @Column(name = "idEjercicio", nullable = false)
    private Integer idEjercicio;

    @Column(name = "respuesta", columnDefinition = "TEXT")
    private String respuesta;

    @Column(name = "correcto", nullable = false)
    private boolean correcto;

    @Column(name = "puntaje", precision = 5, scale = 2)
    private BigDecimal puntaje;

    @Column(name = "fechaIntento", nullable = false)
    private LocalDateTime fechaIntento;

    @PrePersist
    public void prePersist() {
        if (fechaIntento == null) {
            fechaIntento = LocalDateTime.now();
        }
    }

    public Integer getIdIntento() {
        return idIntento;
    }

    public void setIdIntento(Integer idIntento) {
        this.idIntento = idIntento;
    }

    public Integer getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(Integer idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public Integer getIdEjercicio() {
        return idEjercicio;
    }

    public void setIdEjercicio(Integer idEjercicio) {
        this.idEjercicio = idEjercicio;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    public boolean isCorrecto() {
        return correcto;
    }

    public void setCorrecto(boolean correcto) {
        this.correcto = correcto;
    }

    public BigDecimal getPuntaje() {
        return puntaje;
    }

    public void setPuntaje(BigDecimal puntaje) {
        this.puntaje = puntaje;
    }

    public LocalDateTime getFechaIntento() {
        return fechaIntento;
    }

    public void setFechaIntento(LocalDateTime fechaIntento) {
        this.fechaIntento = fechaIntento;
    }
}
