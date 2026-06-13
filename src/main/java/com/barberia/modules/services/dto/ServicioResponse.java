package com.barberia.modules.services.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ServicioResponse {

    private final Integer id;
    private final String nombre;
    private final String categoria;
    private final BigDecimal precio;
    private final Integer duracionMinutos;
    private final String descripcion;
    private final Boolean estado;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
