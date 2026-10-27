package com.mundomanitas.dao;

import com.mundomanitas.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UsuarioDao extends GenericDao<Usuario, Integer> {

    Optional<Usuario> buscarPorEmail(String email);

    Optional<Usuario> buscarPorIdConRol(Integer id);

    List<Usuario> listarConRol();

    List<Usuario> listarPorRol(String codigo);

    Page<Usuario> buscar(String q, Pageable pageable);

    Optional<Usuario> buscarPorToken(String token);

    boolean existeEmail(String email);

    long contarActivos();

    long contarPorRol(String codigo);

    long contarActivosPorRol(String codigo);
}
