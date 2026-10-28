package com.mundomanitas.dto.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VideoRequest(
    @NotNull(message = "El módulo es obligatorio.")
    Integer idModulo,

    @NotBlank(message = "El título es obligatorio.")
    @Size(max = 100, message = "El título no puede superar los 100 caracteres.")
    String titulo,

    @NotBlank(message = "La URL es obligatoria.")
    @Size(max = 255, message = "La URL no puede superar los 255 caracteres.")
    String url,

    Integer duracion,
    Integer orden
) {
}
