package com.mundomanitas.dto.view;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Anuncio listo para mostrar en el aula o en el panel del docente.
 */
public class AnuncioView {

    private static final DateTimeFormatter FECHA =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Integer idAnuncio;
    private final Integer idModulo;
    private final String titulo;
    private final String mensaje;
    private final String autor;
    private final LocalDateTime fecha;
    private final boolean activo;

    public AnuncioView(
        Integer idAnuncio,
        Integer idModulo,
        String titulo,
        String mensaje,
        String autor,
        LocalDateTime fecha,
        boolean activo
    ) {
        this.idAnuncio = idAnuncio;
        this.idModulo = idModulo;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.autor = autor;
        this.fecha = fecha;
        this.activo = activo;
    }

    public Integer getIdAnuncio() {
        return idAnuncio;
    }

    public Integer getIdModulo() {
        return idModulo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getAutor() {
        return autor;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getFechaTexto() {
        return fecha == null ? "" : fecha.format(FECHA);
    }

    public boolean isActivo() {
        return activo;
    }
}
