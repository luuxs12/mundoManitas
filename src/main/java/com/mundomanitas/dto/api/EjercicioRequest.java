package com.mundomanitas.dto.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EjercicioRequest(
    @NotNull Integer idModulo,
    @NotBlank String titulo,
    @NotBlank String tipo,
    String contenido,
    String respuestaCorrecta
) {
}
