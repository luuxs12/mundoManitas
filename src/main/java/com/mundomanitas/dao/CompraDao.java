package com.mundomanitas.dao;

import com.mundomanitas.entity.Compra;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Contrato DAO de las compras de cursos premium.
 */
public interface CompraDao extends GenericDao<Compra, Integer> {

    Optional<Compra> buscar(Integer idEstudiante, Integer idModulo);

    boolean existe(Integer idEstudiante, Integer idModulo);

    List<Compra> listarPorEstudiante(Integer idEstudiante);

    List<Compra> listarPorModulo(Integer idModulo);

    long contarPorModulo(Integer idModulo);

    BigDecimal totalInvertido(Integer idEstudiante);
}
