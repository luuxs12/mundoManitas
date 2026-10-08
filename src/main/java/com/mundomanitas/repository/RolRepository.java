package com.mundomanitas.repository;

import com.mundomanitas.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Integer> {

    Optional<Rol> findByCodigoIgnoreCase(String codigo);
}
