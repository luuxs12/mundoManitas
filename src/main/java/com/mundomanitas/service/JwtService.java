package com.mundomanitas.service;

import com.mundomanitas.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Generación y validación de tokens JWT firmados con HMAC-SHA.
 */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMilisegundos;

    public JwtService(
        @Value("${app.jwt.secret}") String secreto,
        @Value("${app.jwt.expiration-minutes:120}") long minutos
    ) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMilisegundos = minutos * 60_000L;
    }

    public String generar(Usuario usuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expiracionMilisegundos);

        return Jwts.builder()
            .subject(usuario.getEmail())
            .claim("nombre", usuario.getNombre())
            .claim("rol", usuario.getRol() == null ? "" : usuario.getRol().getCodigo())
            .issuedAt(ahora)
            .expiration(expiracion)
            .signWith(clave)
            .compact();
    }

    public String extraerEmail(String token) {
        return parsear(token).getSubject();
    }

    public boolean esValido(String token) {
        try {
            parsear(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public long getExpiracionSegundos() {
        return expiracionMilisegundos / 1000L;
    }

    private Claims parsear(String token) {
        return Jwts.parser()
            .verifyWith(clave)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
