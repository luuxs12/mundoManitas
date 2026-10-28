package com.mundomanitas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Compra de un curso premium por parte de un estudiante.
 *
 * <p>El pago es simulado: la compra se registra con su monto, referencia y
 * fecha para que quede auditable y para habilitar el contenido del módulo.
 * La restricción única impide comprar dos veces el mismo curso.</p>
 */
@Entity
@Table(
    name = "compra",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_compra_estudiante_modulo",
        columnNames = {"idEstudiante", "idModulo"}
    )
)
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCompra")
    private Integer idCompra;

    @Column(name = "idEstudiante", nullable = false)
    private Integer idEstudiante;

    @Column(name = "idModulo", nullable = false)
    private Integer idModulo;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "PAGADA";

    @Column(name = "referencia", nullable = false, length = 60)
    private String referencia;

    @Column(name = "metodoPago", nullable = false, length = 30)
    private String metodoPago = "SIMULADO";

    @Column(name = "fechaCompra", nullable = false)
    private LocalDateTime fechaCompra;

    @PrePersist
    public void prePersist() {
        if (fechaCompra == null) {
            fechaCompra = LocalDateTime.now();
        }
        if (estado == null || estado.isBlank()) {
            estado = "PAGADA";
        }
        if (metodoPago == null || metodoPago.isBlank()) {
            metodoPago = "SIMULADO";
        }
    }

    public Integer getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(Integer idCompra) {
        this.idCompra = idCompra;
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

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public LocalDateTime getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(LocalDateTime fechaCompra) {
        this.fechaCompra = fechaCompra;
    }
}
