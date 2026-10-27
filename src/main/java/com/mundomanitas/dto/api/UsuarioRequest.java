package com.mundomanitas.dto.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioRequest(
    @NotBlank String nombre,
    @NotBlank @Email String email,
    String contrasena,
    @NotBlank String rol,
    Boolean activo
) {
}
