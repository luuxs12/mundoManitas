package com.mundomanitas.repository;

import com.mundomanitas.entity.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a las reseñas de los cursos.
 */
public interface ResenaRepository extends JpaRepository<Resena, Integer> {

    Optional<Resena> findByIdEstudianteAndIdModulo(Integer idEstudiante, Integer idModulo);

    boolean existsByIdEstudianteAndIdModulo(Integer idEstudiante, Integer idModulo);

    List<Resena> findByIdModuloOrderByFechaCreacionDesc(Integer idModulo);

    long countByIdModulo(Integer idModulo);

    long countByIdEstudiante(Integer idEstudiante);

    @Query("select avg(r.calificacion) from Resena r where r.idModulo = :idModulo")
    Double promedioPorModulo(@Param("idModulo") Integer idModulo);

    @Query("""
        select count(r) from Resena r, Modulo m
        where r.idModulo = m.idModulo and m.idDocente = :idDocente
        """)
    long contarPorDocente(@Param("idDocente") Integer idDocente);

    @Query("""
        select avg(r.calificacion) from Resena r, Modulo m
        where r.idModulo = m.idModulo and m.idDocente = :idDocente
        """)
    Double promedioPorDocente(@Param("idDocente") Integer idDocente);

    void deleteByIdModulo(Integer idModulo);
}
