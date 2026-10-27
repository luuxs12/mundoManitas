package com.mundomanitas.dto.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VideoRequest(
    @NotNull Integer idModulo,
    @NotBlank String titulo,
    @NotBlank String url,
    Integer duracion,
    Integer orden
) {
}
