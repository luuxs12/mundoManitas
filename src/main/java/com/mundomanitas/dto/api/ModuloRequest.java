package com.mundomanitas.dto.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * Datos para crear o editar un módulo.
 *
 * <p>El constructor de cuatro argumentos se conserva para las llamadas que
 * solo editan el contenido base; en ese caso el módulo queda como gratuito.</p>
 */
public record ModuloRequest(
    @NotBlank String titulo,
    String descripcion,
    @NotBlank String nivel,
    Integer orden,
    Boolean premium,
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo") BigDecimal precio
) {

    public ModuloRequest(
        String titulo,
        String descripcion,
        String nivel,
        Integer orden
    ) {
        this(titulo, descripcion, nivel, orden, false, null);
    }

    public boolean esPremium() {
        return Boolean.TRUE.equals(premium);
    }
}
