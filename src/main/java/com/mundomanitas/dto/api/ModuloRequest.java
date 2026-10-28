package com.mundomanitas.dto.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Datos para crear o editar un módulo.
 *
 * <p>El constructor de cuatro argumentos se conserva para las llamadas que
 * solo editan el contenido base; en ese caso el módulo queda como gratuito.</p>
 */
public record ModuloRequest(
    @NotBlank(message = "El título es obligatorio.")
    @Size(min = 4, max = 100, message = "El título debe tener entre 4 y 100 caracteres.")
    String titulo,

    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres.")
    String descripcion,

    @NotBlank(message = "El nivel es obligatorio.")
    String nivel,

    Integer orden,

    Boolean premium,

    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    BigDecimal precio
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
