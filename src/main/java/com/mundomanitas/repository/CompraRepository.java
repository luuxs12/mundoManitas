package com.mundomanitas.repository;

import com.mundomanitas.entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a las compras de cursos premium.
 */
public interface CompraRepository extends JpaRepository<Compra, Integer> {

    Optional<Compra> findByIdEstudianteAndIdModulo(Integer idEstudiante, Integer idModulo);

    boolean existsByIdEstudianteAndIdModulo(Integer idEstudiante, Integer idModulo);

    List<Compra> findByIdEstudianteOrderByFechaCompraDesc(Integer idEstudiante);

    List<Compra> findByIdModuloOrderByFechaCompraDesc(Integer idModulo);

    long countByIdModulo(Integer idModulo);

    @Query("select coalesce(sum(c.monto), 0) from Compra c where c.idEstudiante = :idEstudiante")
    BigDecimal totalInvertidoPorEstudiante(@Param("idEstudiante") Integer idEstudiante);
}
