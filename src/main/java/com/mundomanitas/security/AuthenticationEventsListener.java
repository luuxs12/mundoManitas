package com.mundomanitas.security;

import com.mundomanitas.service.AuditoriaService;
import com.mundomanitas.service.UsuarioService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationFailureLockedEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * Escucha los eventos de Spring Security para llevar el contador de
 * intentos fallidos, aplicar el bloqueo temporal de cuentas y registrar
 * la auditoría de accesos.
 */
@Component
public class AuthenticationEventsListener {

    private final UsuarioService usuarioService;
    private final AuditoriaService auditoria;

    public AuthenticationEventsListener(
        UsuarioService usuarioService,
        AuditoriaService auditoria
    ) {
        this.usuarioService = usuarioService;
        this.auditoria = auditoria;
    }

    @EventListener
    public void alAutenticar(AuthenticationSuccessEvent event) {
        usuarioService.registrarAccesoExitoso(event.getAuthentication().getName());
    }

    @EventListener
    public void alFallar(AbstractAuthenticationFailureEvent event) {
        String email = String.valueOf(event.getAuthentication().getName());

        if (event instanceof AuthenticationFailureBadCredentialsEvent) {
            usuarioService.registrarIntentoFallido(email);
            return;
        }

        if (event instanceof AuthenticationFailureLockedEvent) {
            auditoria.registrar(
                "LOGIN_BLOQUEADO",
                email,
                "la cuenta está bloqueada temporalmente"
            );
        }
    }
}
