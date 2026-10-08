package com.mundomanitas.service;

import com.mundomanitas.entity.ModuloSistema;
import com.mundomanitas.entity.Usuario;
import com.mundomanitas.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class NavegacionService {

    private final UsuarioRepository usuarioRepository;

    public NavegacionService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<ModuloSistema> obtenerModulos(String email) {
        Usuario usuario = usuarioRepository
            .findByEmailIgnoreCase(email)
            .orElseThrow();

        return usuario.getRol().getModulos().stream()
            .filter(ModuloSistema::isActivo)
            .sorted(Comparator.comparing(ModuloSistema::getOrden))
            .toList();
    }
}
