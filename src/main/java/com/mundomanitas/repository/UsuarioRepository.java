package com.mundomanitas.repository;

import com.mundomanitas.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    long countByActivoTrue();

    long countByRolCodigoIgnoreCase(String codigo);
}
