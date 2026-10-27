package com.mundomanitas.service;

import com.mundomanitas.dto.api.LoginRequest;
import com.mundomanitas.dto.api.LoginResponse;
import com.mundomanitas.entity.Usuario;
import com.mundomanitas.exception.ReglaNegocioException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Autenticación para la API REST. Reutiliza el AuthenticationManager de
 * Spring Security y emite un JWT firmado tras validar las credenciales.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    public AuthService(
        AuthenticationManager authenticationManager,
        UsuarioService usuarioService,
        JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
            )
        );

        Usuario usuario = usuarioService.buscarPorEmail(authentication.getName())
            .orElseThrow(() -> new ReglaNegocioException(
                "No fue posible recuperar el usuario autenticado."
            ));

        String token = jwtService.generar(usuario);

        return new LoginResponse(
            token,
            "Bearer",
            usuario.getNombre(),
            jwtService.getExpiracionSegundos()
        );
    }
}
