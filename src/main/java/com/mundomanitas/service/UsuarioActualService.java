package com.mundomanitas.service;

import com.mundomanitas.dao.UsuarioDao;
import com.mundomanitas.entity.Usuario;
import com.mundomanitas.exception.RecursoNoEncontradoException;
import com.mundomanitas.exception.ReglaNegocioException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resuelve el usuario autenticado de la petición actual y expone
 * comprobaciones de rol reutilizables por controladores y servicios.
 */
@Service
public class UsuarioActualService {

    private final UsuarioDao usuarioDao;

    public UsuarioActualService(UsuarioDao usuarioDao) {
        this.usuarioDao = usuarioDao;
    }

    @Transactional(readOnly = true)
    public Usuario obtener(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RecursoNoEncontradoException("No hay una sesión activa.");
        }

        return usuarioDao.buscarPorEmail(authentication.getName())
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    @Transactional(readOnly = true)
    public boolean esEstudiante(Authentication authentication) {
        return "estudiante".equalsIgnoreCase(obtener(authentication).getTipo());
    }

    @Transactional(readOnly = true)
    public Integer idEstudiante(Authentication authentication) {
        Usuario usuario = obtener(authentication);

        if (!"estudiante".equalsIgnoreCase(usuario.getTipo())) {
            throw new ReglaNegocioException(
                "Esta acción está disponible únicamente para estudiantes."
            );
        }

        return usuario.getIdUsuario();
    }

    @Transactional(readOnly = true)
    public String email(Authentication authentication) {
        return obtener(authentication).getEmail();
    }
}
