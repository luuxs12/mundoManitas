package com.mundomanitas.repository;

import com.mundomanitas.entity.ModuloSistema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModuloSistemaRepository extends JpaRepository<ModuloSistema, Integer> {

    Optional<ModuloSistema> findByCodigoIgnoreCase(String codigo);

    long countByActivoTrue();
}
