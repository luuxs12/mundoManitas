package com.mundomanitas.dto.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
    @NotBlank(message = "El nombre es obligatorio.")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres.")
    String nombre,

    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "El correo no es válido.")
    @Size(max = 100, message = "El correo no puede superar los 100 caracteres.")
    String email,

    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.")
    String contrasena,

    @NotBlank(message = "El rol es obligatorio.")
    String rol,

    Boolean activo
) {
}
