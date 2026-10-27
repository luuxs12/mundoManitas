package com.mundomanitas.dao;

import com.mundomanitas.entity.Ejercicio;
import com.mundomanitas.entity.Modulo;
import com.mundomanitas.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * DAO agregado del contenido educativo (módulos, videos y ejercicios).
 */
public interface CursoDao {

    List<Modulo> listarModulos();

    List<Modulo> listarModulosPorDocente(Integer idDocente);

    long contarModulosPorDocente(Integer idDocente);

    Page<Modulo> buscarModulos(String q, Pageable pageable);

    Optional<Modulo> buscarModulo(Integer idModulo);

    Modulo guardarModulo(Modulo modulo);

    void eliminarModulo(Integer idModulo);

    boolean existeTituloModulo(String titulo);

    int siguienteOrdenModulo();

    List<Video> listarVideos(Integer idModulo);

    int siguienteOrdenVideo(Integer idModulo);

    Optional<Video> buscarVideo(Integer idVideo);

    Video guardarVideo(Video video);

    void eliminarVideo(Integer idVideo);

    void eliminarVideosDeModulo(Integer idModulo);

    List<Ejercicio> listarEjercicios(Integer idModulo);

    Optional<Ejercicio> buscarEjercicio(Integer idEjercicio);

    Ejercicio guardarEjercicio(Ejercicio ejercicio);

    void eliminarEjercicio(Integer idEjercicio);

    void eliminarEjerciciosDeModulo(Integer idModulo);

    long contarModulos();

    long contarVideos();

    long contarEjercicios();
}
