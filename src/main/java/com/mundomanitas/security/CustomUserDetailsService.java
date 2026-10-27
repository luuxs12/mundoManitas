package com.mundomanitas.security;

import com.mundomanitas.entity.ModuloSistema;
import com.mundomanitas.entity.Usuario;
import com.mundomanitas.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String email = username.trim();

        Usuario usuario = usuarioRepository
            .findByEmailIgnoreCase(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getCodigo()));

        usuario.getRol().getModulos().stream()
            .filter(ModuloSistema::isActivo)
            .map(modulo -> new SimpleGrantedAuthority("MODULO_" + modulo.getCodigo()))
            .forEach(authorities::add);

        return User.builder()
            .username(usuario.getEmail())
            .password(usuario.getContrasena())
            .authorities(authorities)
            .disabled(!usuario.isActivo() || !usuario.getRol().isActivo())
            .accountLocked(usuario.isBloqueado())
            .build();
    }
}
