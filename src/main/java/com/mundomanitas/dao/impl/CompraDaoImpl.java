package com.mundomanitas.dao.impl;

import com.mundomanitas.dao.AbstractDao;
import com.mundomanitas.dao.CompraDao;
import com.mundomanitas.entity.Compra;
import com.mundomanitas.repository.CompraRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public class CompraDaoImpl extends AbstractDao<Compra, Integer> implements CompraDao {

    private final CompraRepository compraRepository;

    public CompraDaoImpl(CompraRepository compraRepository) {
        super(compraRepository);
        this.compraRepository = compraRepository;
    }

    @Override
    public Optional<Compra> buscar(Integer idEstudiante, Integer idModulo) {
        return compraRepository.findByIdEstudianteAndIdModulo(idEstudiante, idModulo);
    }

    @Override
    public boolean existe(Integer idEstudiante, Integer idModulo) {
        return compraRepository.existsByIdEstudianteAndIdModulo(idEstudiante, idModulo);
    }

    @Override
    public List<Compra> listarPorEstudiante(Integer idEstudiante) {
        return compraRepository.findByIdEstudianteOrderByFechaCompraDesc(idEstudiante);
    }

    @Override
    public List<Compra> listarPorModulo(Integer idModulo) {
        return compraRepository.findByIdModuloOrderByFechaCompraDesc(idModulo);
    }

    @Override
    public long contarPorModulo(Integer idModulo) {
        return compraRepository.countByIdModulo(idModulo);
    }

    @Override
    public BigDecimal totalInvertido(Integer idEstudiante) {
        return compraRepository.totalInvertidoPorEstudiante(idEstudiante);
    }
}
