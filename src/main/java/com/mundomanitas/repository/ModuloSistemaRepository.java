package com.mundomanitas.repository;

import com.mundomanitas.entity.ModuloSistema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModuloSistemaRepository extends JpaRepository<ModuloSistema, Integer> {

    Optional<ModuloSistema> findByCodigoIgnoreCase(String codigo);

    List<ModuloSistema> findAllByOrderByOrdenAsc();

    long countByActivoTrue();
}
