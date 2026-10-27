package com.mundomanitas.repository;

import com.mundomanitas.entity.IntentoEjercicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IntentoEjercicioRepository extends JpaRepository<IntentoEjercicio, Integer> {

    List<IntentoEjercicio> findByIdEstudianteAndIdEjercicioOrderByFechaIntentoDesc(
        Integer idEstudiante,
        Integer idEjercicio
    );

    Optional<IntentoEjercicio> findFirstByIdEstudianteAndIdEjercicioOrderByFechaIntentoDesc(
        Integer idEstudiante,
        Integer idEjercicio
    );

    @Query("""
        select ie.idEjercicio from IntentoEjercicio ie
        where ie.idEstudiante = :idEstudiante and ie.correcto = true
        """)
    List<Integer> idsEjerciciosCorrectos(@Param("idEstudiante") Integer idEstudiante);

    @Query("""
        select count(distinct ie.idEjercicio) from IntentoEjercicio ie, Ejercicio e
        where ie.idEjercicio = e.idEjercicio
          and ie.idEstudiante = :idEstudiante
          and e.idModulo = :idModulo
          and ie.correcto = true
        """)
    long contarEjerciciosCorrectosPorModulo(
        @Param("idEstudiante") Integer idEstudiante,
        @Param("idModulo") Integer idModulo
    );

    @Query("""
        select avg(ie.puntaje) from IntentoEjercicio ie, Ejercicio e
        where ie.idEjercicio = e.idEjercicio
          and ie.idEstudiante = :idEstudiante
          and e.idModulo = :idModulo
        """)
    Double promedioPuntajePorModulo(
        @Param("idEstudiante") Integer idEstudiante,
        @Param("idModulo") Integer idModulo
    );

    void deleteByIdEjercicio(Integer idEjercicio);
}
