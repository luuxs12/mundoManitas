package com.mundomanitas.web;

import com.mundomanitas.exception.RecursoNoEncontradoException;
import com.mundomanitas.exception.ReglaNegocioException;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Manejador global de excepciones HTTP.
 * Intercepta errores 400, 403, 404, 405 y 500 y los renderiza con vistas
 * Thymeleaf en lugar del Whitelabel Error Page por defecto de Spring Boot.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(Exception ex, Model model) {
        model.addAttribute("status", HttpStatus.NOT_FOUND.value());
        model.addAttribute("mensaje", "El recurso que buscas no existe o fue movido.");
        return "error/404";
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleRecursoNoEncontrado(
        RecursoNoEncontradoException ex,
        Model model
    ) {
        model.addAttribute("status", HttpStatus.NOT_FOUND.value());
        model.addAttribute("mensaje", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(ReglaNegocioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleReglaNegocio(ReglaNegocioException ex, Model model) {
        model.addAttribute("status", HttpStatus.BAD_REQUEST.value());
        model.addAttribute("mensaje", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatus(
        ResponseStatusException ex,
        Model model,
        HttpServletResponse response
    ) {
        int status = ex.getStatusCode().value();

        response.setStatus(status);

        model.addAttribute("status", status);
        model.addAttribute(
            "mensaje",
            ex.getReason() != null ? ex.getReason() : mensajePorDefecto(status)
        );

        return vistaPorEstado(status);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(Model model) {
        model.addAttribute("status", HttpStatus.FORBIDDEN.value());
        model.addAttribute("mensaje", "No tienes permisos para acceder a este recurso.");
        return "error/403";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(Model model) {
        model.addAttribute("status", HttpStatus.BAD_REQUEST.value());
        model.addAttribute("mensaje", "La solicitud contiene un parámetro inválido.");
        return "error/error";
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public String handleMethodNotSupported(Model model) {
        model.addAttribute("status", HttpStatus.METHOD_NOT_ALLOWED.value());
        model.addAttribute("mensaje", "El método HTTP utilizado no está permitido para este recurso.");
        return "error/error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneric(Exception ex, Model model) {
        log.error("Error no controlado en la aplicación", ex);

        model.addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        model.addAttribute("mensaje", "Ocurrió un error interno. Por favor intenta de nuevo.");
        return "error/error";
    }

    private String vistaPorEstado(int status) {
        return switch (status) {
            case 403 -> "error/403";
            case 404 -> "error/404";
            default -> "error/error";
        };
    }

    private String mensajePorDefecto(int status) {
        return switch (status) {
            case 403 -> "No tienes permisos para acceder a este recurso.";
            case 404 -> "El recurso que buscas no existe o fue movido.";
            default -> "Ocurrió un error interno. Por favor intenta de nuevo.";
        };
    }
}
