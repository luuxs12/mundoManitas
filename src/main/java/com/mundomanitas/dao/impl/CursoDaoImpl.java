package com.mundomanitas.dao.impl;

import com.mundomanitas.dao.CursoDao;
import com.mundomanitas.entity.Ejercicio;
import com.mundomanitas.entity.Modulo;
import com.mundomanitas.entity.Video;
import com.mundomanitas.repository.EjercicioRepository;
import com.mundomanitas.repository.ModuloRepository;
import com.mundomanitas.repository.VideoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CursoDaoImpl implements CursoDao {

    private final ModuloRepository moduloRepository;
    private final VideoRepository videoRepository;
    private final EjercicioRepository ejercicioRepository;

    public CursoDaoImpl(
        ModuloRepository moduloRepository,
        VideoRepository videoRepository,
        EjercicioRepository ejercicioRepository
    ) {
        this.moduloRepository = moduloRepository;
        this.videoRepository = videoRepository;
        this.ejercicioRepository = ejercicioRepository;
    }

    @Override
    public List<Modulo> listarModulos() {
        return moduloRepository.findAllByOrderByOrdenAsc();
    }

    @Override
    public List<Modulo> listarModulosPorDocente(Integer idDocente) {
        return moduloRepository.findByIdDocenteOrderByOrdenAsc(idDocente);
    }

    @Override
    public long contarModulosPorDocente(Integer idDocente) {
        return moduloRepository.countByIdDocente(idDocente);
    }

    @Override
    public Page<Modulo> buscarModulos(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return moduloRepository.findAll(pageable);
        }
        String filtro = q.trim();
        return moduloRepository
            .findByTituloContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
                filtro,
                filtro,
                pageable
            );
    }

    @Override
    public Optional<Modulo> buscarModulo(Integer idModulo) {
        return moduloRepository.findById(idModulo);
    }

    @Override
    public Modulo guardarModulo(Modulo modulo) {
        return moduloRepository.save(modulo);
    }

    @Override
    public void eliminarModulo(Integer idModulo) {
        moduloRepository.deleteById(idModulo);
    }

    @Override
    public boolean existeTituloModulo(String titulo) {
        return moduloRepository.existsByTituloIgnoreCase(titulo);
    }

    @Override
    public int siguienteOrdenModulo() {
        return moduloRepository.findFirstByOrderByOrdenDesc()
            .map(modulo -> modulo.getOrden() + 1)
            .orElse(1);
    }

    @Override
    public List<Video> listarVideos(Integer idModulo) {
        return videoRepository.findByIdModuloOrderByOrdenAsc(idModulo);
    }

    @Override
    public int siguienteOrdenVideo(Integer idModulo) {
        return videoRepository.findFirstByIdModuloOrderByOrdenDesc(idModulo)
            .map(video -> video.getOrden() + 1)
            .orElse(1);
    }

    @Override
    public Optional<Video> buscarVideo(Integer idVideo) {
        return videoRepository.findById(idVideo);
    }

    @Override
    public Video guardarVideo(Video video) {
        return videoRepository.save(video);
    }

    @Override
    public void eliminarVideo(Integer idVideo) {
        videoRepository.deleteById(idVideo);
    }

    @Override
    public void eliminarVideosDeModulo(Integer idModulo) {
        videoRepository.deleteByIdModulo(idModulo);
    }

    @Override
    public List<Ejercicio> listarEjercicios(Integer idModulo) {
        return ejercicioRepository.findByIdModuloOrderByIdEjercicioAsc(idModulo);
    }

    @Override
    public Optional<Ejercicio> buscarEjercicio(Integer idEjercicio) {
        return ejercicioRepository.findById(idEjercicio);
    }

    @Override
    public Ejercicio guardarEjercicio(Ejercicio ejercicio) {
        return ejercicioRepository.save(ejercicio);
    }

    @Override
    public void eliminarEjercicio(Integer idEjercicio) {
        ejercicioRepository.deleteById(idEjercicio);
    }

    @Override
    public void eliminarEjerciciosDeModulo(Integer idModulo) {
        ejercicioRepository.deleteByIdModulo(idModulo);
    }

    @Override
    public long contarModulos() {
        return moduloRepository.count();
    }

    @Override
    public long contarVideos() {
        return videoRepository.count();
    }

    @Override
    public long contarEjercicios() {
        return ejercicioRepository.count();
    }
}
