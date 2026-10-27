package com.mundomanitas.dto.api;

import jakarta.validation.constraints.NotBlank;

public record IntentoRequest(
    @NotBlank String respuesta
) {
}
