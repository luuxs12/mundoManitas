package com.mundomanitas.dao.impl;

import com.mundomanitas.dao.AbstractDao;
import com.mundomanitas.dao.InscripcionDao;
import com.mundomanitas.entity.Inscripcion;
import com.mundomanitas.repository.InscripcionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class InscripcionDaoImpl extends AbstractDao<Inscripcion, Integer> implements InscripcionDao {

    private final InscripcionRepository inscripcionRepository;

    public InscripcionDaoImpl(InscripcionRepository inscripcionRepository) {
        super(inscripcionRepository);
        this.inscripcionRepository = inscripcionRepository;
    }

    @Override
    public List<Inscripcion> listarPorEstudiante(Integer idEstudiante) {
        return inscripcionRepository.findByIdEstudiante(idEstudiante);
    }

    @Override
    public List<Inscripcion> listarPorModulo(Integer idModulo) {
        return inscripcionRepository.findByIdModulo(idModulo);
    }

    @Override
    public long contarPorModulo(Integer idModulo) {
        return inscripcionRepository.countByIdModulo(idModulo);
    }

    @Override
    public Optional<Inscripcion> buscar(Integer idEstudiante, Integer idModulo) {
        return inscripcionRepository.findByIdEstudianteAndIdModulo(idEstudiante, idModulo);
    }

    @Override
    public boolean existe(Integer idEstudiante, Integer idModulo) {
        return inscripcionRepository.existsByIdEstudianteAndIdModulo(idEstudiante, idModulo);
    }

    @Override
    public long contarPorEstudiante(Integer idEstudiante) {
        return inscripcionRepository.countByIdEstudiante(idEstudiante);
    }

    @Override
    public void eliminar(Integer idEstudiante, Integer idModulo) {
        inscripcionRepository.deleteByIdEstudianteAndIdModulo(idEstudiante, idModulo);
    }

    @Override
    public void eliminarPorModulo(Integer idModulo) {
        inscripcionRepository.deleteByIdModulo(idModulo);
    }
}
