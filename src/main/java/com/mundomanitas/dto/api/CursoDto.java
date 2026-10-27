package com.mundomanitas.dto.api;

public record CursoDto(
    Integer idModulo,
    String titulo,
    String descripcion,
    String nivel,
    Integer orden,
    int porcentaje,
    boolean inscrito,
    String estado
) {
}
