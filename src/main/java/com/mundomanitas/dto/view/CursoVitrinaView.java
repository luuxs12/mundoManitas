package com.mundomanitas.dto.view;

import java.math.BigDecimal;

/**
 * Datos mínimos de un curso para la vitrina pública de la página de inicio.
 */
public record CursoVitrinaView(
    Integer idModulo,
    String titulo,
    String descripcion,
    String nivel,
    String nivelClave,
    String inicial,
    int videos,
    int ejercicios,
    boolean premium,
    BigDecimal precio
) {
}
