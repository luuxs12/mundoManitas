package com.mundomanitas.dao.impl;

import com.mundomanitas.dao.ProgresoDao;
import com.mundomanitas.entity.IntentoEjercicio;
import com.mundomanitas.entity.Progreso;
import com.mundomanitas.entity.VideoProgreso;
import com.mundomanitas.repository.IntentoEjercicioRepository;
import com.mundomanitas.repository.ProgresoRepository;
import com.mundomanitas.repository.VideoProgresoRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public class ProgresoDaoImpl implements ProgresoDao {

    private final ProgresoRepository progresoRepository;
    private final VideoProgresoRepository videoProgresoRepository;
    private final IntentoEjercicioRepository intentoEjercicioRepository;

    public ProgresoDaoImpl(
        ProgresoRepository progresoRepository,
        VideoProgresoRepository videoProgresoRepository,
        IntentoEjercicioRepository intentoEjercicioRepository
    ) {
        this.progresoRepository = progresoRepository;
        this.videoProgresoRepository = videoProgresoRepository;
        this.intentoEjercicioRepository = intentoEjercicioRepository;
    }

    @Override
    public List<Progreso> listarPorEstudiante(Integer idEstudiante) {
        return progresoRepository.findByIdEstudiante(idEstudiante);
    }

    @Override
    public List<Progreso> listarTodos() {
        return progresoRepository.findAll();
    }

    @Override
    public List<Progreso> listarPorModulo(Integer idModulo) {
        return progresoRepository.findByIdModulo(idModulo);
    }

    @Override
    public Double promedioPorModulo(Integer idModulo) {
        return progresoRepository.promedioPorcentajePorModulo(idModulo);
    }

    @Override
    public Double promedioGlobal() {
        return progresoRepository.promedioPorcentajeGlobal();
    }

    @Override
    public Optional<Progreso> buscar(Integer idEstudiante, Integer idModulo) {
        return progresoRepository.findByIdEstudianteAndIdModulo(idEstudiante, idModulo);
    }

    @Override
    public Progreso guardar(Progreso progreso) {
        return progresoRepository.save(progreso);
    }

    @Override
    public List<VideoProgreso> listarVideoProgreso(Integer idEstudiante) {
        return videoProgresoRepository.findByIdEstudiante(idEstudiante);
    }

    @Override
    public Optional<VideoProgreso> buscarVideoProgreso(Integer idEstudiante, Integer idVideo) {
        return videoProgresoRepository.findByIdEstudianteAndIdVideo(idEstudiante, idVideo);
    }

    @Override
    public VideoProgreso guardarVideoProgreso(VideoProgreso videoProgreso) {
        return videoProgresoRepository.save(videoProgreso);
    }

    @Override
    public long contarVideosCompletados(Integer idEstudiante, Integer idModulo) {
        return videoProgresoRepository.contarCompletadosPorModulo(idEstudiante, idModulo);
    }

    @Override
    public List<Integer> idsVideosCompletados(Integer idEstudiante) {
        return videoProgresoRepository.idsVideosCompletados(idEstudiante);
    }

    @Override
    public IntentoEjercicio guardarIntento(IntentoEjercicio intento) {
        return intentoEjercicioRepository.save(intento);
    }

    @Override
    public Optional<IntentoEjercicio> ultimoIntento(Integer idEstudiante, Integer idEjercicio) {
        return intentoEjercicioRepository
            .findFirstByIdEstudianteAndIdEjercicioOrderByFechaIntentoDesc(
                idEstudiante,
                idEjercicio
            );
    }

    @Override
    public long contarEjerciciosCorrectos(Integer idEstudiante, Integer idModulo) {
        return intentoEjercicioRepository
            .contarEjerciciosCorrectosPorModulo(idEstudiante, idModulo);
    }

    @Override
    public List<Integer> idsEjerciciosCorrectos(Integer idEstudiante) {
        return intentoEjercicioRepository.idsEjerciciosCorrectos(idEstudiante);
    }

    @Override
    public Double promedioPuntaje(Integer idEstudiante, Integer idModulo) {
        return intentoEjercicioRepository.promedioPuntajePorModulo(idEstudiante, idModulo);
    }

    @Override
    public long contarAprobados() {
        return progresoRepository.countByPorcentajeGreaterThanEqual(new BigDecimal("100"));
    }

    @Override
    public void eliminarVideoProgresoPorVideo(Integer idVideo) {
        videoProgresoRepository.deleteByIdVideo(idVideo);
    }

    @Override
    public void eliminarIntentosPorEjercicio(Integer idEjercicio) {
        intentoEjercicioRepository.deleteByIdEjercicio(idEjercicio);
    }

    @Override
    public void eliminarProgresoPorModulo(Integer idModulo) {
        progresoRepository.deleteByIdModulo(idModulo);
    }
}
