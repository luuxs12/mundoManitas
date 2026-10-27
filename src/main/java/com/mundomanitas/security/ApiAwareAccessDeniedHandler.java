package com.mundomanitas.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Responde 403 en JSON para la API y delega en la página de error
 * personalizada para las vistas MVC.
 */
@Component
public class ApiAwareAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException accessDeniedException
    ) throws IOException {

        if (esApi(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                "{\"error\":\"Acceso denegado\",\"mensaje\":\"No tienes permisos para realizar esta acción.\"}"
            );
            return;
        }

        response.sendError(
            HttpServletResponse.SC_FORBIDDEN,
            "No tienes permisos para acceder a este recurso."
        );
    }

    private boolean esApi(HttpServletRequest request) {
        String ruta = request.getRequestURI();
        String contexto = request.getContextPath();

        if (contexto != null && !contexto.isEmpty() && ruta.startsWith(contexto)) {
            ruta = ruta.substring(contexto.length());
        }

        return ruta.startsWith("/api/");
    }
}
