package com.mundomanitas.web;

import com.mundomanitas.service.NavegacionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class NavegacionControllerAdvice {

    private final NavegacionService navegacionService;

    public NavegacionControllerAdvice(NavegacionService navegacionService) {
        this.navegacionService = navegacionService;
    }

    @ModelAttribute("modulosSidebar")
    public Object modulosSidebar(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return List.of();
        }

        return navegacionService.obtenerModulos(authentication.getName());
    }

    @ModelAttribute("rutaActual")
    public String rutaActual(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
