package com.mundomanitas.api;

import com.mundomanitas.dto.api.ApiError;
import com.mundomanitas.exception.RecursoNoEncontradoException;
import com.mundomanitas.exception.ReglaNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce las excepciones de la API REST a respuestas JSON coherentes.
 * Se aplica únicamente a los controladores del paquete com.mundomanitas.api
 * para no interferir con las vistas Thymeleaf.
 */
@RestControllerAdvice(basePackages = "com.mundomanitas.api")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> noEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ApiError("Recurso no encontrado", ex.getMessage()));
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ApiError> reglaNegocio(ReglaNegocioException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ApiError("Solicitud inválida", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> detalles = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
            detalles.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ApiError(
                "Datos inválidos",
                "Revisa los campos enviados.",
                detalles
            ));
    }

    @ExceptionHandler({BadCredentialsException.class, LockedException.class, DisabledException.class})
    public ResponseEntity<ApiError> credenciales(Exception ex) {
        String mensaje = ex instanceof LockedException
            ? "La cuenta está bloqueada temporalmente. Intenta más tarde."
            : ex instanceof DisabledException
                ? "La cuenta está desactivada."
                : "Correo o contraseña incorrectos.";

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(new ApiError("No autenticado", mensaje));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> accesoDenegado(AccessDeniedException ex) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(new ApiError(
                "Acceso denegado",
                "No tienes permisos para realizar esta acción."
            ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> errorGenerico(Exception ex) {
        log.error("Error no controlado en la API", ex);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiError(
                "Error interno",
                "Ocurrió un error inesperado. Intenta de nuevo."
            ));
    }
}
