package com.mundomanitas.repository;

import com.mundomanitas.entity.Anuncio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Acceso a los anuncios publicados en los cursos.
 */
public interface AnuncioRepository extends JpaRepository<Anuncio, Integer> {

    List<Anuncio> findByIdModuloAndActivoTrueOrderByFechaPublicacionDesc(Integer idModulo);

    boolean existsByIdModuloAndTituloIgnoreCase(Integer idModulo, String titulo);

    List<Anuncio> findByIdModuloOrderByFechaPublicacionDesc(Integer idModulo);

    long countByIdModuloAndActivoTrue(Integer idModulo);

    @Query("""
        select count(a) from Anuncio a, Modulo m
        where a.idModulo = m.idModulo and m.idDocente = :idDocente and a.activo = true
        """)
    long contarActivosPorDocente(@Param("idDocente") Integer idDocente);

    List<Anuncio> findTop6ByActivoTrueOrderByFechaPublicacionDesc();

    void deleteByIdModulo(Integer idModulo);
}
