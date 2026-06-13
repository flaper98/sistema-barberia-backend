package com.barberia.security.auth;

import com.barberia.exception.BusinessException;
import com.barberia.modules.audit.AuditAction;
import com.barberia.modules.audit.AuditPublisher;
import com.barberia.modules.users.entity.RefreshToken;
import com.barberia.modules.users.entity.Usuario;
import com.barberia.modules.users.repository.RefreshTokenRepository;
import com.barberia.modules.users.repository.UsuarioRepository;
import com.barberia.security.JwtService;
import com.barberia.security.auth.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditPublisher auditPublisher;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException ex) {
            auditPublisher.publish(AuditAction.LOGIN_FAIL, request.getEmail(),
                    "USUARIO", "N/A", "Login fallido: " + ex.getClass().getSimpleName());
            throw ex;
        }

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        String accessToken = jwtService.generateAccessToken(usuario);
        String refreshToken = createRefreshToken(usuario);

        usuarioRepository.updateUltimoAcceso(usuario.getId(), LocalDateTime.now());

        auditPublisher.publish(AuditAction.LOGIN_OK, usuario.getEmail(),
                "USUARIO", usuario.getId().toString(), "Login exitoso");
        log.info("Login exitoso para usuario ID={} rol={}", usuario.getId(), usuario.getRol());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtExpiration)
                .userId(usuario.getId())
                .nombre(usuario.getNombreCompleto())
                .email(usuario.getEmail())
                .rol(usuario.getRol().name())
                .avatar(usuario.getAvatar())
                .build();
    }

    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException("Refresh token inválido"));

        if (!storedToken.isValid()) {
            storedToken.setRevocado(true);
            refreshTokenRepository.save(storedToken);
            throw new BusinessException("Refresh token expirado o revocado. Inicia sesión nuevamente.");
        }

        Usuario usuario = storedToken.getUsuario();
        if (!Boolean.TRUE.equals(usuario.getEstado())) {
            throw new BusinessException("Usuario deshabilitado");
        }

        storedToken.setRevocado(true);
        refreshTokenRepository.save(storedToken);

        String newAccessToken  = jwtService.generateAccessToken(usuario);
        String newRefreshToken = createRefreshToken(usuario);

        log.info("Token renovado para usuario ID={}", usuario.getId());

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtExpiration)
                .userId(usuario.getId())
                .nombre(usuario.getNombreCompleto())
                .email(usuario.getEmail())
                .rol(usuario.getRol().name())
                .avatar(usuario.getAvatar())
                .build();
    }

    @Transactional
    public void logout(Integer usuarioId) {
        refreshTokenRepository.revokeAllByUsuarioId(usuarioId);
        auditPublisher.publish(AuditAction.LOGOUT, "ID:" + usuarioId,
                "USUARIO", usuarioId.toString(), "Logout ejecutado");
        log.info("Logout ejecutado para usuario ID={}", usuarioId);
    }

    public AuthMeResponse getMe(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return AuthMeResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .nombreCompleto(usuario.getNombreCompleto())
                .email(usuario.getEmail())
                .rol(usuario.getRol().name())
                .avatar(usuario.getAvatar())
                .estado(usuario.getEstado())
                .ultimoAcceso(usuario.getUltimoAcceso())
                .createdAt(usuario.getCreatedAt())
                .build();
    }

    private String createRefreshToken(Usuario usuario) {
        refreshTokenRepository.revokeAllByUsuarioId(usuario.getId());

        RefreshToken token = RefreshToken.builder()
                .usuario(usuario)
                .token(UUID.randomUUID().toString())
                .expiraAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000))
                .revocado(false)
                .build();

        return refreshTokenRepository.save(token).getToken();
    }
}
