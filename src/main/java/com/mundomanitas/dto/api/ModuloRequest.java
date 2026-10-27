package com.mundomanitas.dto.api;

import jakarta.validation.constraints.NotBlank;

public record ModuloRequest(
    @NotBlank String titulo,
    String descripcion,
    @NotBlank String nivel,
    Integer orden
) {
}
