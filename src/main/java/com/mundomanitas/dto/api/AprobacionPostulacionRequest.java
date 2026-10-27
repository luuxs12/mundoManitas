package com.mundomanitas.dto.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Datos que el administrador envía para aprobar una postulación docente:
 * módulo a asignar y contraseña inicial de la nueva cuenta.
 */
public record AprobacionPostulacionRequest(
    @NotNull Integer idModulo,
    @NotBlank String contrasena,
    String observacion
) {
}
