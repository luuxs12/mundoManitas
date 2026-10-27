package com.mundomanitas.dao;

import com.mundomanitas.entity.IntentoEjercicio;
import com.mundomanitas.entity.Progreso;
import com.mundomanitas.entity.VideoProgreso;

import java.util.List;
import java.util.Optional;

/**
 * DAO agregado del progreso académico: avance por módulo, videos
 * completados e intentos de ejercicios.
 */
public interface ProgresoDao {

    List<Progreso> listarPorEstudiante(Integer idEstudiante);

    List<Progreso> listarTodos();

    List<Progreso> listarPorModulo(Integer idModulo);

    Double promedioPorModulo(Integer idModulo);

    Double promedioGlobal();

    Optional<Progreso> buscar(Integer idEstudiante, Integer idModulo);

    Progreso guardar(Progreso progreso);

    List<VideoProgreso> listarVideoProgreso(Integer idEstudiante);

    Optional<VideoProgreso> buscarVideoProgreso(Integer idEstudiante, Integer idVideo);

    VideoProgreso guardarVideoProgreso(VideoProgreso videoProgreso);

    long contarVideosCompletados(Integer idEstudiante, Integer idModulo);

    List<Integer> idsVideosCompletados(Integer idEstudiante);

    IntentoEjercicio guardarIntento(IntentoEjercicio intento);

    Optional<IntentoEjercicio> ultimoIntento(Integer idEstudiante, Integer idEjercicio);

    long contarEjerciciosCorrectos(Integer idEstudiante, Integer idModulo);

    List<Integer> idsEjerciciosCorrectos(Integer idEstudiante);

    Double promedioPuntaje(Integer idEstudiante, Integer idModulo);

    long contarAprobados();

    void eliminarVideoProgresoPorVideo(Integer idVideo);

    void eliminarIntentosPorEjercicio(Integer idEjercicio);

    void eliminarProgresoPorModulo(Integer idModulo);
}
