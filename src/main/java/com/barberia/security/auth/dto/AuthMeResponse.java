package com.barberia.security.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "Información del usuario autenticado actualmente")
public class AuthMeResponse {

    private final Integer id;
    private final String nombre;
    private final String apellido;
    private final String nombreCompleto;
    private final String email;
    private final String rol;
    private final String avatar;
    private final Integer barberoId;
    private final Boolean estado;
    private final LocalDateTime ultimoAcceso;
    private final LocalDateTime createdAt;
}
