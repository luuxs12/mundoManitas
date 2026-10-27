package com.mundomanitas.api;

import com.mundomanitas.dto.api.LoginRequest;
import com.mundomanitas.dto.api.LoginResponse;
import com.mundomanitas.dto.api.MensajeResponse;
import com.mundomanitas.dto.api.RecuperacionRequest;
import com.mundomanitas.dto.api.RestablecerRequest;
import com.mundomanitas.service.AuthService;
import com.mundomanitas.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    public AuthApiController(AuthService authService, UsuarioService usuarioService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/recuperar")
    public ResponseEntity<MensajeResponse> recuperar(
        @Valid @RequestBody RecuperacionRequest request
    ) {
        usuarioService.solicitarRecuperacion(request.email());

        return ResponseEntity.ok(new MensajeResponse(
            "Si el correo está registrado, enviamos un enlace de recuperación."
        ));
    }

    @PostMapping("/restablecer")
    public ResponseEntity<MensajeResponse> restablecer(
        @Valid @RequestBody RestablecerRequest request
    ) {
        usuarioService.restablecer(request.token(), request.contrasena());

        return ResponseEntity.ok(new MensajeResponse(
            "La contraseña se actualizó correctamente."
        ));
    }
}
