package com.barberia.modules.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "Datos del usuario del sistema (sin contraseña ni datos sensibles)")
public class UsuarioResponse {

    private final Integer id;
    private final String nombre;
    private final String apellido;
    private final String nombreCompleto;
    private final String email;
    private final String rol;
    private final Boolean estado;
    private final String avatar;
    private final LocalDateTime ultimoAcceso;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
