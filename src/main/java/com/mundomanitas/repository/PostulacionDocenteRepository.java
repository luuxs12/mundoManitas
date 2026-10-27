package com.mundomanitas.repository;

import com.mundomanitas.entity.EstadoPostulacion;
import com.mundomanitas.entity.PostulacionDocente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a las postulaciones docentes. Las consultas por correo permiten
 * impedir postulaciones duplicadas mientras la anterior sigue pendiente.
 */
public interface PostulacionDocenteRepository extends JpaRepository<PostulacionDocente, Integer> {

    List<PostulacionDocente> findAllByOrderByFechaPostulacionDesc();

    List<PostulacionDocente> findByEstadoOrderByFechaPostulacionDesc(EstadoPostulacion estado);

    long countByEstado(EstadoPostulacion estado);

    boolean existsByEmailIgnoreCaseAndEstado(String email, EstadoPostulacion estado);

    Optional<PostulacionDocente> findFirstByEmailIgnoreCaseAndEstadoOrderByFechaPostulacionDesc(
        String email,
        EstadoPostulacion estado
    );

    Optional<PostulacionDocente> findFirstByEmailIgnoreCaseOrderByFechaPostulacionDesc(String email);
}
