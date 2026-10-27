package com.mundomanitas.dto.api;

import com.mundomanitas.entity.PostulacionDocente;

import java.time.format.DateTimeFormatter;

/**
 * Respuesta pública tras registrar una postulación. No expone datos
 * internos de revisión; solo confirma el folio y el estado inicial.
 */
public record PostulacionResponse(
    Integer id,
    String nombre,
    String email,
    String estado,
    String fechaPostulacion
) {

    private static final DateTimeFormatter FORMATO =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static PostulacionResponse desde(PostulacionDocente postulacion) {
        return new PostulacionResponse(
            postulacion.getIdPostulacion(),
            postulacion.getNombre(),
            postulacion.getEmail(),
            postulacion.getEstado().name(),
            postulacion.getFechaPostulacion() == null
                ? ""
                : postulacion.getFechaPostulacion().format(FORMATO)
        );
    }
}
