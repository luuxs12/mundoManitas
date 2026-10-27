package com.mundomanitas.dao.impl;

import com.mundomanitas.dao.AbstractDao;
import com.mundomanitas.dao.UsuarioDao;
import com.mundomanitas.entity.Usuario;
import com.mundomanitas.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioDaoImpl extends AbstractDao<Usuario, Integer> implements UsuarioDao {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDaoImpl(UsuarioRepository usuarioRepository) {
        super(usuarioRepository);
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email);
    }

    @Override
    public Optional<Usuario> buscarPorIdConRol(Integer id) {
        return usuarioRepository.buscarConRolPorId(id);
    }

    @Override
    public List<Usuario> listarConRol() {
        return usuarioRepository.listarConRol();
    }

    @Override
    public List<Usuario> listarPorRol(String codigo) {
        return usuarioRepository.listarPorRol(codigo);
    }

    @Override
    public Page<Usuario> buscar(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return usuarioRepository.listarConRol(pageable);
        }
        return usuarioRepository.buscarConRol(q.trim(), pageable);
    }

    @Override
    public Optional<Usuario> buscarPorToken(String token) {
        return usuarioRepository.findByTokenRecuperacion(token);
    }

    @Override
    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public long contarActivos() {
        return usuarioRepository.countByActivoTrue();
    }

    @Override
    public long contarPorRol(String codigo) {
        return usuarioRepository.countByRolCodigoIgnoreCase(codigo);
    }

    @Override
    public long contarActivosPorRol(String codigo) {
        return usuarioRepository.contarActivosPorRol(codigo);
    }
}
