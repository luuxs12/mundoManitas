package com.mundomanitas.dto.view;

import java.util.List;

/**
 * Resumen del catálogo que se muestra en la página pública de inicio.
 */
public record VitrinaView(
    List<CursoVitrinaView> cursos,
    long modulos,
    long videos,
    long ejercicios
) {
}
