package com.mundomanitas.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Curso guardado por un estudiante en su lista de favoritos.
 *
 * <p>La lista es personal y no habilita el contenido: sirve para que el
 * estudiante pueda volver a un curso que le interesa, incluso si todavía
 * no se ha inscrito o si es premium y aún no lo compró.</p>
 */
@Entity
@Table(
    name = "favorito",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_favorito_estudiante_modulo",
        columnNames = {"idEstudiante", "idModulo"}
    )
)
public class Favorito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idFavorito")
    private Integer idFavorito;

    @Column(name = "idEstudiante", nullable = false)
    private Integer idEstudiante;

    @Column(name = "idModulo", nullable = false)
    private Integer idModulo;

    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    public Integer getIdFavorito() {
        return idFavorito;
    }

    public void setIdFavorito(Integer idFavorito) {
        this.idFavorito = idFavorito;
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

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
