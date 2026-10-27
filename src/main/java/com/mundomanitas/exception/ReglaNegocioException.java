package com.mundomanitas.exception;

/**
 * Se lanza cuando una operación viola una regla de negocio (HTTP 400).
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
