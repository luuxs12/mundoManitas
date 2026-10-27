package com.mundomanitas.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Implementación base del DAO genérico sobre Spring Data JPA.
 * Las implementaciones concretas heredan el CRUD y agregan consultas.
 */
public abstract class AbstractDao<T, ID> implements GenericDao<T, ID> {

    private final JpaRepository<T, ID> repository;

    protected AbstractDao(JpaRepository<T, ID> repository) {
        this.repository = repository;
    }

    protected JpaRepository<T, ID> repositorio() {
        return repository;
    }

    @Override
    public T guardar(T entidad) {
        return repository.save(entidad);
    }

    @Override
    public Optional<T> buscarPorId(ID id) {
        return repository.findById(id);
    }

    @Override
    public List<T> listarTodos() {
        return repository.findAll();
    }

    @Override
    public void eliminar(T entidad) {
        repository.delete(entidad);
    }

    @Override
    public void eliminarPorId(ID id) {
        repository.deleteById(id);
    }

    @Override
    public long contar() {
        return repository.count();
    }
}
