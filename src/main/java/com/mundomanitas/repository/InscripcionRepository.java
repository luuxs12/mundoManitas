package com.mundomanitas.repository;

import com.mundomanitas.entity.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {

    List<Inscripcion> findByIdEstudiante(Integer idEstudiante);

    List<Inscripcion> findByIdModulo(Integer idModulo);

    Optional<Inscripcion> findByIdEstudianteAndIdModulo(Integer idEstudiante, Integer idModulo);

    boolean existsByIdEstudianteAndIdModulo(Integer idEstudiante, Integer idModulo);

    long countByIdEstudiante(Integer idEstudiante);

    long countByIdModulo(Integer idModulo);

    void deleteByIdEstudianteAndIdModulo(Integer idEstudiante, Integer idModulo);

    void deleteByIdModulo(Integer idModulo);
}
