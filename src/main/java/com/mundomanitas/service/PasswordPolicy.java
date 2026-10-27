package com.mundomanitas.service;

import com.mundomanitas.exception.PasswordPolicyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Política de contraseñas para altas, cambios y recuperación.
 * No se aplica al inicio de sesión para no romper cuentas históricas.
 */
@Component
public class PasswordPolicy {

    private final int longitudMinima;

    public PasswordPolicy(
        @Value("${app.security.password-min-length:8}") int longitudMinima
    ) {
        this.longitudMinima = longitudMinima;
    }

    public void validar(String contrasena) {
        List<String> errores = new ArrayList<>();

        if (contrasena == null || contrasena.length() < longitudMinima) {
            errores.add("debe tener al menos " + longitudMinima + " caracteres");
        } else {
            if (contrasena.chars().noneMatch(Character::isUpperCase)) {
                errores.add("debe incluir una letra mayúscula");
            }
            if (contrasena.chars().noneMatch(Character::isLowerCase)) {
                errores.add("debe incluir una letra minúscula");
            }
            if (contrasena.chars().noneMatch(Character::isDigit)) {
                errores.add("debe incluir un número");
            }
            if (contrasena.matches("[A-Za-z0-9]+")) {
                errores.add("debe incluir un carácter especial");
            }
        }

        if (!errores.isEmpty()) {
            throw new PasswordPolicyException(
                "La contraseña no cumple la política de seguridad: "
                    + String.join(", ", errores) + "."
            );
        }
    }

    public int getLongitudMinima() {
        return longitudMinima;
    }
}
