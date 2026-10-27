package com.mundomanitas.dto.api;

import java.util.Map;

public record ApiError(
    String error,
    String mensaje,
    Map<String, String> detalles
) {
    public ApiError(String error, String mensaje) {
        this(error, mensaje, Map.of());
    }
}
