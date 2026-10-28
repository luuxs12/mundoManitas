package com.mundomanitas.repository;

import com.mundomanitas.entity.Favorito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a los cursos favoritos de cada estudiante.
 */
public interface FavoritoRepository extends JpaRepository<Favorito, Integer> {

    Optional<Favorito> findByIdEstudianteAndIdModulo(Integer idEstudiante, Integer idModulo);

    boolean existsByIdEstudianteAndIdModulo(Integer idEstudiante, Integer idModulo);

    List<Favorito> findByIdEstudianteOrderByFechaCreacionDesc(Integer idEstudiante);

    long countByIdEstudiante(Integer idEstudiante);

    void deleteByIdEstudianteAndIdModulo(Integer idEstudiante, Integer idModulo);

    void deleteByIdModulo(Integer idModulo);
}
