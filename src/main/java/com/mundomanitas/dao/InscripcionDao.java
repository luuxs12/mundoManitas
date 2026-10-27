package com.mundomanitas.dao;

import com.mundomanitas.entity.Inscripcion;

import java.util.List;
import java.util.Optional;

public interface InscripcionDao extends GenericDao<Inscripcion, Integer> {

    List<Inscripcion> listarPorEstudiante(Integer idEstudiante);

    List<Inscripcion> listarPorModulo(Integer idModulo);

    long contarPorModulo(Integer idModulo);

    Optional<Inscripcion> buscar(Integer idEstudiante, Integer idModulo);

    boolean existe(Integer idEstudiante, Integer idModulo);

    long contarPorEstudiante(Integer idEstudiante);

    void eliminar(Integer idEstudiante, Integer idModulo);

    void eliminarPorModulo(Integer idModulo);
}
