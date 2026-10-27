package com.mundomanitas.entity;

/**
 * Ciclo de vida de una postulación docente.
 *
 * <p>Una postulación nace en {@link #PENDIENTE} y solo el administrador
 * puede resolverla. La aprobación crea la cuenta DOCENTE y asigna un
 * módulo; el rechazo conserva la postulación como historial auditable.</p>
 */
public enum EstadoPostulacion {

    PENDIENTE("Pendiente de revisión"),
    APROBADA("Aprobada"),
    RECHAZADA("Rechazada");

    private final String etiqueta;

    EstadoPostulacion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public boolean estaPendiente() {
        return this == PENDIENTE;
    }

    public boolean estaResuelta() {
        return this == APROBADA || this == RECHAZADA;
    }

    public static EstadoPostulacion desde(String valor) {
        if (valor == null || valor.isBlank()) {
            return PENDIENTE;
        }

        for (EstadoPostulacion estado : values()) {
            if (estado.name().equalsIgnoreCase(valor.trim())) {
                return estado;
            }
        }

        return PENDIENTE;
    }
}
