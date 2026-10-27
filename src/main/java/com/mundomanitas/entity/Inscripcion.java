package com.mundomanitas.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Relación entre un estudiante y un módulo educativo.
 * Permite distinguir "módulo disponible" de "módulo inscrito".
 */
@Entity
@Table(
    name = "inscripcion",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_inscripcion_estudiante_modulo",
        columnNames = {"idEstudiante", "idModulo"}
    )
)
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idInscripcion")
    private Integer idInscripcion;

    @Column(name = "idEstudiante", nullable = false)
    private Integer idEstudiante;

    @Column(name = "idModulo", nullable = false)
    private Integer idModulo;

    @Column(name = "fechaInscripcion", nullable = false)
    private LocalDateTime fechaInscripcion;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "ACTIVA";

    @PrePersist
    public void prePersist() {
        if (fechaInscripcion == null) {
            fechaInscripcion = LocalDateTime.now();
        }
        if (estado == null || estado.isBlank()) {
            estado = "ACTIVA";
        }
    }

    public Integer getIdInscripcion() {
        return idInscripcion;
    }

    public void setIdInscripcion(Integer idInscripcion) {
        this.idInscripcion = idInscripcion;
    }

    public Integer getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(Integer idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public Integer getIdModulo() {
        return idModulo;
    }

    public void setIdModulo(Integer idModulo) {
        this.idModulo = idModulo;
    }

    public LocalDateTime getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(LocalDateTime fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
