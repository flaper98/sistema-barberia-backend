package com.barberia.modules.services.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder(toBuilder = true)
public class ServicioResponse {

    private final Integer id;
    private final String nombre;
    private final String categoria;
    private final BigDecimal precio;
    private final Boolean precioVariable;
    private final Integer duracionMinutos;
    private final String descripcion;
    private final Boolean estado;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<ServicioCortesiaDto> cortesias;
}
