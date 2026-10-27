package com.mundomanitas.dto.api;

import java.time.LocalDateTime;

public record InscripcionDto(
    Integer idInscripcion,
    Integer idModulo,
    String tituloModulo,
    LocalDateTime fechaInscripcion,
    String estado
) {
}
