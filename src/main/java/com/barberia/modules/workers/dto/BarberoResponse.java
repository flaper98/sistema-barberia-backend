package com.barberia.modules.workers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "Datos del barbero")
public class BarberoResponse {

    private final Integer id;
    private final String nombre;
    private final String apellido;
    private final String nombreCompleto;
    private final String telefono;
    private final String especialidad;
    private final BigDecimal totalGenerado;
    private final Integer serviciosRealizados;
    private final Boolean estado;
    private final String avatar;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
