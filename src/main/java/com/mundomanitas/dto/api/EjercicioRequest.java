package com.mundomanitas.dto.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EjercicioRequest(
    @NotNull(message = "El módulo es obligatorio.")
    Integer idModulo,

    @NotBlank(message = "El título es obligatorio.")
    @Size(max = 100, message = "El título no puede superar los 100 caracteres.")
    String titulo,

    @NotBlank(message = "El tipo es obligatorio.")
    String tipo,

    String contenido,
    String respuestaCorrecta
) {
}
