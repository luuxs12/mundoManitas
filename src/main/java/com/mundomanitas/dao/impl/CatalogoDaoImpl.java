package com.mundomanitas.dao.impl;

import com.mundomanitas.dao.CatalogoDao;
import com.mundomanitas.entity.Estudiante;
import com.mundomanitas.entity.ModuloSistema;
import com.mundomanitas.entity.Rol;
import com.mundomanitas.repository.EstudianteRepository;
import com.mundomanitas.repository.ModuloSistemaRepository;
import com.mundomanitas.repository.RolRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CatalogoDaoImpl implements CatalogoDao {

    private final RolRepository rolRepository;
    private final ModuloSistemaRepository moduloSistemaRepository;
    private final EstudianteRepository estudianteRepository;

    public CatalogoDaoImpl(
        RolRepository rolRepository,
        ModuloSistemaRepository moduloSistemaRepository,
        EstudianteRepository estudianteRepository
    ) {
        this.rolRepository = rolRepository;
        this.moduloSistemaRepository = moduloSistemaRepository;
        this.estudianteRepository = estudianteRepository;
    }

    @Override
    public List<Rol> listarRoles() {
        return rolRepository.findAllByOrderByNombreAsc();
    }

    @Override
    public Optional<Rol> buscarRol(String codigo) {
        return rolRepository.findByCodigoIgnoreCase(codigo);
    }

    @Override
    public Optional<Rol> buscarRolPorId(Integer idRol) {
        return rolRepository.findById(idRol);
    }

    @Override
    public Rol guardarRol(Rol rol) {
        return rolRepository.save(rol);
    }

    @Override
    public List<ModuloSistema> listarModulosSistema() {
        return moduloSistemaRepository.findAllByOrderByOrdenAsc();
    }

    @Override
    public long contarRoles() {
        return rolRepository.count();
    }

    @Override
    public long contarModulosSistemaActivos() {
        return moduloSistemaRepository.countByActivoTrue();
    }

    @Override
    public Optional<Estudiante> buscarEstudiante(Integer idEstudiante) {
        return estudianteRepository.findById(idEstudiante);
    }

    @Override
    public Estudiante guardarEstudiante(Estudiante estudiante) {
        return estudianteRepository.save(estudiante);
    }

    @Override
    public boolean existeEstudiante(Integer idEstudiante) {
        return estudianteRepository.existsById(idEstudiante);
    }
}
