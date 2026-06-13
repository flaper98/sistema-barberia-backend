package com.barberia.security.auth;

import com.barberia.common.ApiResponse;
import com.barberia.security.CustomUserDetails;
import com.barberia.security.auth.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login, logout y gestión de tokens JWT")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y devuelve access token + refresh token")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Inicio de sesión exitoso", response));
    }

    @Operation(summary = "Renovar token", description = "Renueva el access token usando un refresh token válido")
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.ok("Token renovado exitosamente", response));
    }

    @Operation(summary = "Cerrar sesión", description = "Revoca todos los refresh tokens del usuario autenticado",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.logout(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok("Sesión cerrada exitosamente"));
    }

    @Operation(summary = "Obtener mi perfil", description = "Devuelve los datos del usuario autenticado actualmente",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AuthMeResponse>> getMe(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        AuthMeResponse response = authService.getMe(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
