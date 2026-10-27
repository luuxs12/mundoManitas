package com.mundomanitas.dto.api;

import jakarta.validation.constraints.NotBlank;

public record RestablecerRequest(
    @NotBlank String token,
    @NotBlank String contrasena
) {
}
