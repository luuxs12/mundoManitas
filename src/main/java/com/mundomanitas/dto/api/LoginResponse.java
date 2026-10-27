package com.mundomanitas.dto.api;

public record LoginResponse(
    String token,
    String tipo,
    String nombre,
    long expiraEnSegundos
) {
}
