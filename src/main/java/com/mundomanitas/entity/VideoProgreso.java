package com.mundomanitas.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Marca que un estudiante completó un video concreto.
 */
@Entity
@Table(
    name = "video_progreso",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_video_progreso_estudiante_video",
        columnNames = {"idEstudiante", "idVideo"}
    )
)
public class VideoProgreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idVideoProgreso")
    private Integer idVideoProgreso;

    @Column(name = "idEstudiante", nullable = false)
    private Integer idEstudiante;

    @Column(name = "idVideo", nullable = false)
    private Integer idVideo;

    @Column(name = "completado", nullable = false)
    private boolean completado = false;

    @Column(name = "fechaCompletado")
    private LocalDateTime fechaCompletado;

    public Integer getIdVideoProgreso() {
        return idVideoProgreso;
    }

    public void setIdVideoProgreso(Integer idVideoProgreso) {
        this.idVideoProgreso = idVideoProgreso;
    }

    public Integer getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(Integer idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public Integer getIdVideo() {
        return idVideo;
    }

    public void setIdVideo(Integer idVideo) {
        this.idVideo = idVideo;
    }

    public boolean isCompletado() {
        return completado;
    }

    public void setCompletado(boolean completado) {
        this.completado = completado;
    }

    public LocalDateTime getFechaCompletado() {
        return fechaCompletado;
    }

    public void setFechaCompletado(LocalDateTime fechaCompletado) {
        this.fechaCompletado = fechaCompletado;
    }
}
