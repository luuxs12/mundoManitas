package com.mundomanitas.dto.api;

public record IntentoResponse(
    boolean correcto,
    String mensaje,
    int porcentaje
) {
}
