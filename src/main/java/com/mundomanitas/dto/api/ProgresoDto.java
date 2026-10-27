package com.mundomanitas.dto.api;

import java.math.BigDecimal;

public record ProgresoDto(
    Integer idModulo,
    String titulo,
    int porcentaje,
    String estado,
    BigDecimal calificacion
) {
}
