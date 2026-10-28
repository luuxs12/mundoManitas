package com.mundomanitas.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Anuncio publicado por el docente de un curso para sus estudiantes.
 *
 * <p>Los anuncios son la vía de comunicación interna del módulo: fechas de
 * entrega, recordatorios o material complementario. Solo el docente dueño
 * del curso (o la administración) puede publicarlos.</p>
 */
@Entity
@Table(name = "anuncio")
public class Anuncio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAnuncio")
    private Integer idAnuncio;

    @Column(name = "idModulo", nullable = false)
    private Integer idModulo;

    @Column(name = "idDocente", nullable = false)
    private Integer idDocente;

    @Column(name = "titulo", nullable = false, length = 120)
    private String titulo;

    @Column(name = "mensaje", nullable = false, length = 1000)
    private String mensaje;

    @Column(name = "fechaPublicacion", nullable = false)
    private LocalDateTime fechaPublicacion;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @PrePersist
    public void prePersist() {
        if (fechaPublicacion == null) {
            fechaPublicacion = LocalDateTime.now();
        }
    }

    public Integer getIdAnuncio() {
        return idAnuncio;
    }

    public void setIdAnuncio(Integer idAnuncio) {
        this.idAnuncio = idAnuncio;
    }

    public Integer getIdModulo() {
        return idModulo;
    }

    public void setIdModulo(Integer idModulo) {
        this.idModulo = idModulo;
    }

    public Integer getIdDocente() {
        return idDocente;
    }

    public void setIdDocente(Integer idDocente) {
        this.idDocente = idDocente;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
