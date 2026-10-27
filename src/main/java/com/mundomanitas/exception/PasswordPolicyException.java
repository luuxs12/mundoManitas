package com.mundomanitas.exception;

/**
 * Se lanza cuando una contraseña no cumple la política de seguridad.
 */
public class PasswordPolicyException extends ReglaNegocioException {

    public PasswordPolicyException(String mensaje) {
        super(mensaje);
    }
}
