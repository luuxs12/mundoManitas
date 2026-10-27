package com.mundomanitas.dto.api;

import jakarta.validation.constraints.NotBlank;

/**
 * Motivo obligatorio para rechazar una postulación docente.
 */
public record RechazoPostulacionRequest(@NotBlank String observacion) {
}
