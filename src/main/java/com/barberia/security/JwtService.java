package com.barberia.security;

import com.barberia.modules.users.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private long expiration;

    public String generateAccessToken(Usuario usuario) {
        return buildToken(
                Map.of("rol", usuario.getRol().name(),
                       "nombre", usuario.getNombreCompleto()),
                usuario.getEmail(),
                expiration
        );
    }

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    public String extractRol(String token) {
        return getClaims(token).get("rol", String.class);
    }

    public boolean isTokenValid(String token, String email) {
        try {
            Claims claims = getClaims(token);
            return email.equals(claims.getSubject()) && !isTokenExpired(claims);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return isTokenExpired(getClaims(token));
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expirationMs) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
