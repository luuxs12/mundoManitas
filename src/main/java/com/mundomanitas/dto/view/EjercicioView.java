package com.mundomanitas.dto.view;

import java.util.List;

public class EjercicioView {

    private final Integer idEjercicio;
    private final String titulo;
    private final String tipo;
    private final String pregunta;
    private final String texto;
    private final List<String> opciones;
    private final boolean resuelto;
    private final String ultimaRespuesta;

    public EjercicioView(
        Integer idEjercicio,
        String titulo,
        String tipo,
        String pregunta,
        String texto,
        List<String> opciones,
        boolean resuelto,
        String ultimaRespuesta
    ) {
        this.idEjercicio = idEjercicio;
        this.titulo = titulo;
        this.tipo = tipo;
        this.pregunta = pregunta;
        this.texto = texto;
        this.opciones = opciones;
        this.resuelto = resuelto;
        this.ultimaRespuesta = ultimaRespuesta;
    }

    public Integer getIdEjercicio() {
        return idEjercicio;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getTipo() {
        return tipo;
    }

    public String getPregunta() {
        return pregunta;
    }

    public String getTexto() {
        return texto;
    }

    public List<String> getOpciones() {
        return opciones;
    }

    public boolean isResuelto() {
        return resuelto;
    }

    public String getUltimaRespuesta() {
        return ultimaRespuesta;
    }
}
