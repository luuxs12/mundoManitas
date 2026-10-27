package com.mundomanitas.repository;

import com.mundomanitas.entity.Modulo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModuloRepository extends JpaRepository<Modulo, Integer> {

    List<Modulo> findAllByOrderByOrdenAsc();

    Page<Modulo> findByTituloContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
        String titulo,
        String descripcion,
        Pageable pageable
    );

    boolean existsByTituloIgnoreCase(String titulo);

    Optional<Modulo> findFirstByOrderByOrdenDesc();

    List<Modulo> findByIdDocenteOrderByOrdenAsc(Integer idDocente);

    long countByIdDocente(Integer idDocente);
}
