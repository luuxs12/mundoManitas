package com.mundomanitas.repository;

import com.mundomanitas.entity.Progreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProgresoRepository extends JpaRepository<Progreso, Integer> {

    List<Progreso> findByIdEstudiante(Integer idEstudiante);

    List<Progreso> findByIdModulo(Integer idModulo);

    Optional<Progreso> findByIdEstudianteAndIdModulo(
        Integer idEstudiante,
        Integer idModulo
    );

    long countByPorcentajeGreaterThanEqual(java.math.BigDecimal porcentaje);

    long countByPorcentaje(Integer porcentaje);

    @Query("select avg(p.porcentaje) from Progreso p where p.idModulo = :idModulo")
    Double promedioPorcentajePorModulo(@Param("idModulo") Integer idModulo);

    @Query("select avg(p.porcentaje) from Progreso p")
    Double promedioPorcentajeGlobal();

    void deleteByIdModulo(Integer idModulo);
}
