package com.mundomanitas.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Responde 401 en JSON para la API y redirige al login en las vistas MVC.
 */
@Component
public class ApiAwareAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {

        if (esApi(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                "{\"error\":\"No autenticado\",\"mensaje\":\"Debes iniciar sesión para acceder a este recurso.\"}"
            );
            return;
        }

        response.sendRedirect(request.getContextPath() + "/login");
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
