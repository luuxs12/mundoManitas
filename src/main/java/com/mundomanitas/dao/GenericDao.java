package com.mundomanitas.dao;

import java.util.List;
import java.util.Optional;

/**
 * Contrato genérico del patrón DAO.
 * Abstrae el acceso a datos para que los servicios no dependan de JPA.
 */
public interface GenericDao<T, ID> {

    T guardar(T entidad);

    Optional<T> buscarPorId(ID id);

    List<T> listarTodos();

    void eliminar(T entidad);

    void eliminarPorId(ID id);

    long contar();
}
