package com.barberia.security.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Respuesta de login con tokens JWT")
public class LoginResponse {

    @Schema(description = "Access token JWT")
    private final String accessToken;

    @Schema(description = "Refresh token para renovar el access token")
    private final String refreshToken;

    @Schema(description = "Tipo de token", example = "Bearer")
    @Builder.Default
    private final String tokenType = "Bearer";

    @Schema(description = "Expiración del access token en milisegundos")
    private final long expiresIn;

    @Schema(description = "ID del usuario autenticado")
    private final Integer userId;

    @Schema(description = "Nombre completo del usuario")
    private final String nombre;

    @Schema(description = "Email del usuario")
    private final String email;

    @Schema(description = "Rol del usuario", example = "ADMIN")
    private final String rol;

    @Schema(description = "URL del avatar")
    private final String avatar;
}
