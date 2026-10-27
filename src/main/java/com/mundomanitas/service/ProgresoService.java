package com.mundomanitas.service;

import com.mundomanitas.dao.CursoDao;
import com.mundomanitas.dao.ProgresoDao;
import com.mundomanitas.dto.api.ProgresoDto;
import com.mundomanitas.entity.Ejercicio;
import com.mundomanitas.entity.Modulo;
import com.mundomanitas.entity.Progreso;
import com.mundomanitas.entity.Video;
import com.mundomanitas.entity.VideoProgreso;
import com.mundomanitas.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reglas de negocio del progreso académico.
 * El porcentaje se calcula automáticamente a partir de videos completados
 * y ejercicios resueltos correctamente.
 */
@Service
public class ProgresoService {

    private final ProgresoDao progresoDao;
    private final CursoDao cursoDao;

    public ProgresoService(ProgresoDao progresoDao, CursoDao cursoDao) {
        this.progresoDao = progresoDao;
        this.cursoDao = cursoDao;
    }

    @Transactional
    public Progreso obtenerOCrear(Integer idEstudiante, Integer idModulo) {
        return progresoDao.buscar(idEstudiante, idModulo).orElseGet(() -> {
            Progreso progreso = new Progreso();
            progreso.setIdEstudiante(idEstudiante);
            progreso.setIdModulo(idModulo);
            progreso.setFechaInicio(LocalDateTime.now());
            progreso.setPorcentaje(BigDecimal.ZERO);
            return progresoDao.guardar(progreso);
        });
    }

    /**
     * Recalcula el avance del estudiante en un módulo.
     * Se conserva el mayor valor alcanzado para no borrar avances previos.
     */
    @Transactional
    public int recalcular(Integer idEstudiante, Integer idModulo) {
        List<Video> videos = cursoDao.listarVideos(idModulo);
        List<Ejercicio> ejercicios = cursoDao.listarEjercicios(idModulo);

        int totalContenidos = videos.size() + ejercicios.size();

        long videosCompletados = progresoDao.contarVideosCompletados(idEstudiante, idModulo);
        long ejerciciosCorrectos = progresoDao.contarEjerciciosCorrectos(idEstudiante, idModulo);

        int calculado = totalContenidos == 0
            ? 0
            : (int) Math.round((videosCompletados + ejerciciosCorrectos) * 100.0 / totalContenidos);

        Progreso progreso = obtenerOCrear(idEstudiante, idModulo);

        int actual = progreso.getPorcentaje() == null
            ? 0
            : progreso.getPorcentaje().intValue();

        int nuevo = Math.min(100, Math.max(actual, calculado));

        progreso.setPorcentaje(BigDecimal.valueOf(nuevo));

        if (nuevo >= 100 && progreso.getFechaCompletado() == null) {
            progreso.setFechaCompletado(LocalDateTime.now());
        }

        Double promedio = progresoDao.promedioPuntaje(idEstudiante, idModulo);
        if (promedio != null) {
            progreso.setCalificacion(
                BigDecimal.valueOf(promedio).setScale(2, RoundingMode.HALF_UP)
            );
        }

        progresoDao.guardar(progreso);

        return nuevo;
    }

    @Transactional
    public void marcarVideoCompletado(Integer idEstudiante, Integer idVideo) {
        Video video = cursoDao.buscarVideo(idVideo)
            .orElseThrow(() -> new RecursoNoEncontradoException("Video no encontrado"));

        VideoProgreso videoProgreso = progresoDao
            .buscarVideoProgreso(idEstudiante, idVideo)
            .orElseGet(() -> {
                VideoProgreso nuevo = new VideoProgreso();
                nuevo.setIdEstudiante(idEstudiante);
                nuevo.setIdVideo(idVideo);
                return nuevo;
            });

        if (!videoProgreso.isCompletado()) {
            videoProgreso.setCompletado(true);
            videoProgreso.setFechaCompletado(LocalDateTime.now());
            progresoDao.guardarVideoProgreso(videoProgreso);
        }

        recalcular(idEstudiante, video.getIdModulo());
    }

    @Transactional(readOnly = true)
    public Optional<Progreso> buscar(Integer idEstudiante, Integer idModulo) {
        return progresoDao.buscar(idEstudiante, idModulo);
    }

    @Transactional(readOnly = true)
    public List<ProgresoDto> listarPorEstudiante(Integer idEstudiante) {
        Map<Integer, Modulo> modulos = cursoDao.listarModulos()
            .stream()
            .collect(Collectors.toMap(Modulo::getIdModulo, Function.identity()));

        return progresoDao.listarPorEstudiante(idEstudiante)
            .stream()
            .map(progreso -> {
                Modulo modulo = modulos.get(progreso.getIdModulo());
                int porcentaje = progreso.getPorcentaje() == null
                    ? 0
                    : progreso.getPorcentaje().intValue();

                return new ProgresoDto(
                    progreso.getIdModulo(),
                    modulo == null ? "Módulo" : modulo.getTitulo(),
                    porcentaje,
                    estado(porcentaje),
                    progreso.getCalificacion()
                );
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public int progresoGeneral(Integer idEstudiante) {
        List<Modulo> modulos = cursoDao.listarModulos();

        if (modulos.isEmpty()) {
            return 0;
        }

        Map<Integer, Progreso> progresos = progresoDao.listarPorEstudiante(idEstudiante)
            .stream()
            .collect(
                Collectors.toMap(
                    Progreso::getIdModulo,
                    Function.identity(),
                    (primero, segundo) -> primero
                )
            );

        int suma = 0;
        for (Modulo modulo : modulos) {
            Progreso progreso = progresos.get(modulo.getIdModulo());
            suma += progreso == null || progreso.getPorcentaje() == null
                ? 0
                : progreso.getPorcentaje().intValue();
        }

        return (int) Math.round((double) suma / modulos.size());
    }

    @Transactional(readOnly = true)
    public long contarAprobados() {
        return progresoDao.contarAprobados();
    }

    private String estado(int porcentaje) {
        if (porcentaje >= 100) {
            return "COMPLETADO";
        }
        if (porcentaje > 0) {
            return "EN PROGRESO";
        }
        return "POR INICIAR";
    }
}
