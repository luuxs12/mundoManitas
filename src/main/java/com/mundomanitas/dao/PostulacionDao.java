package com.mundomanitas.dao;

import com.mundomanitas.entity.EstadoPostulacion;
import com.mundomanitas.entity.PostulacionDocente;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia de postulaciones docentes. Mantiene la misma
 * separación DAO/servicio que el resto de la aplicación para que la capa
 * de negocio no dependa de Spring Data JPA.
 */
public interface PostulacionDao extends GenericDao<PostulacionDocente, Integer> {

    List<PostulacionDocente> listarPorFechaDescendente();

    List<PostulacionDocente> listarPorEstado(EstadoPostulacion estado);

    long contarPorEstado(EstadoPostulacion estado);

    boolean existePendientePorEmail(String email);

    Optional<PostulacionDocente> buscarUltimaPorEmail(String email);
}
