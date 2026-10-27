package com.mundomanitas.dto.api;

/**
 * DTO de usuario. Además de los accesores del record se exponen getters
 * JavaBean para que las vistas Thymeleaf puedan resolver las propiedades
 * (usuario.nombre, usuario.rol, ...) sin ambigüedad.
 */
public record UsuarioDto(
    Integer id,
    String nombre,
    String email,
    String rol,
    boolean activo
) {
    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }
}
