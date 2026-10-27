package com.mundomanitas.repository;

import com.mundomanitas.entity.Rol;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Integer> {

    @EntityGraph(attributePaths = "modulos")
    Optional<Rol> findByCodigoIgnoreCase(String codigo);

    List<Rol> findAllByOrderByNombreAsc();
}
