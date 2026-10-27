package com.mundomanitas.dao;

import com.mundomanitas.entity.Estudiante;
import com.mundomanitas.entity.ModuloSistema;
import com.mundomanitas.entity.Rol;

import java.util.List;
import java.util.Optional;

/**
 * DAO de catálogos: roles, módulos del sistema y perfiles de estudiante.
 */
public interface CatalogoDao {

    List<Rol> listarRoles();

    Optional<Rol> buscarRol(String codigo);

    Optional<Rol> buscarRolPorId(Integer idRol);

    Rol guardarRol(Rol rol);

    List<ModuloSistema> listarModulosSistema();

    long contarRoles();

    long contarModulosSistemaActivos();

    Optional<Estudiante> buscarEstudiante(Integer idEstudiante);

    Estudiante guardarEstudiante(Estudiante estudiante);

    boolean existeEstudiante(Integer idEstudiante);
}
