package com.mundomanitas.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mundomanitas.dao.CursoDao;
import com.mundomanitas.dao.ProgresoDao;
import com.mundomanitas.dto.api.IntentoResponse;
import com.mundomanitas.dto.view.EjercicioView;
import com.mundomanitas.entity.Ejercicio;
import com.mundomanitas.entity.IntentoEjercicio;
import com.mundomanitas.exception.RecursoNoEncontradoException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Resolución y calificación de ejercicios.
 * El contenido se guarda como JSON en la columna TEXT.
 */
@Service
public class EjercicioService {

    private final CursoDao cursoDao;
    private final ProgresoDao progresoDao;
    private final ProgresoService progresoService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EjercicioService(
        CursoDao cursoDao,
        ProgresoDao progresoDao,
        ProgresoService progresoService
    ) {
        this.cursoDao = cursoDao;
        this.progresoDao = progresoDao;
        this.progresoService = progresoService;
    }

    @Transactional
    public IntentoResponse responder(
        Integer idEstudiante,
        Integer idEjercicio,
        String respuesta
    ) {
        Ejercicio ejercicio = cursoDao.buscarEjercicio(idEjercicio)
            .orElseThrow(() -> new RecursoNoEncontradoException("Ejercicio no encontrado"));

        String esperada = extraerRespuesta(ejercicio.getRespuestaCorrecta());
        boolean correcto = normalizar(esperada).equals(normalizar(respuesta));

        IntentoEjercicio intento = new IntentoEjercicio();
        intento.setIdEstudiante(idEstudiante);
        intento.setIdEjercicio(idEjercicio);
        intento.setRespuesta(respuesta);
        intento.setCorrecto(correcto);
        intento.setPuntaje(correcto ? BigDecimal.valueOf(20) : BigDecimal.ZERO);
        progresoDao.guardarIntento(intento);

        int porcentaje = progresoService.recalcular(idEstudiante, ejercicio.getIdModulo());

        String mensaje = correcto
            ? "¡Respuesta correcta! Ejercicio completado."
            : "Respuesta incorrecta. Revisa el contenido e inténtalo de nuevo.";

        return new IntentoResponse(correcto, mensaje, porcentaje);
    }

    @Transactional(readOnly = true)
    public EjercicioView toView(Ejercicio ejercicio, Integer idEstudiante) {
        Map<String, Object> contenido = parsear(ejercicio.getContenido());

        boolean resuelto = false;
        String ultimaRespuesta = null;

        if (idEstudiante != null) {
            Optional<IntentoEjercicio> ultimo = progresoDao
                .ultimoIntento(idEstudiante, ejercicio.getIdEjercicio());

            if (ultimo.isPresent()) {
                resuelto = ultimo.get().isCorrecto();
                ultimaRespuesta = ultimo.get().getRespuesta();
            }
        }

        return new EjercicioView(
            ejercicio.getIdEjercicio(),
            ejercicio.getTitulo(),
            ejercicio.getTipo(),
            texto(contenido.get("pregunta")),
            texto(contenido.get("texto")),
            opciones(contenido.get("opciones")),
            resuelto,
            ultimaRespuesta
        );
    }

    public String extraerRespuesta(String json) {
        return texto(parsear(json).get("respuesta"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parsear(String json) {
        if (StringUtils.isBlank(json)) {
            return Map.of();
        }

        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception ex) {
            return Map.of();
        }
    }

    private String texto(Object valor) {
        return valor == null ? "" : String.valueOf(valor);
    }

    private List<String> opciones(Object valor) {
        if (valor instanceof List<?> lista) {
            return lista.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private String normalizar(String valor) {
        return StringUtils.stripAccents(StringUtils.defaultString(valor))
            .trim()
            .toLowerCase(Locale.ROOT);
    }
}
