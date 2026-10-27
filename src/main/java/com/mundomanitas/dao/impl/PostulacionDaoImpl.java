package com.mundomanitas.dao.impl;

import com.mundomanitas.dao.AbstractDao;
import com.mundomanitas.dao.PostulacionDao;
import com.mundomanitas.entity.EstadoPostulacion;
import com.mundomanitas.entity.PostulacionDocente;
import com.mundomanitas.repository.PostulacionDocenteRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PostulacionDaoImpl
    extends AbstractDao<PostulacionDocente, Integer>
    implements PostulacionDao {

    private final PostulacionDocenteRepository repository;

    public PostulacionDaoImpl(PostulacionDocenteRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    public List<PostulacionDocente> listarPorFechaDescendente() {
        return repository.findAllByOrderByFechaPostulacionDesc();
    }

    @Override
    public List<PostulacionDocente> listarPorEstado(EstadoPostulacion estado) {
        return repository.findByEstadoOrderByFechaPostulacionDesc(estado);
    }

    @Override
    public long contarPorEstado(EstadoPostulacion estado) {
        return repository.countByEstado(estado);
    }

    @Override
    public boolean existePendientePorEmail(String email) {
        return repository.existsByEmailIgnoreCaseAndEstado(email, EstadoPostulacion.PENDIENTE);
    }

    @Override
    public Optional<PostulacionDocente> buscarUltimaPorEmail(String email) {
        return repository.findFirstByEmailIgnoreCaseOrderByFechaPostulacionDesc(email);
    }
}
