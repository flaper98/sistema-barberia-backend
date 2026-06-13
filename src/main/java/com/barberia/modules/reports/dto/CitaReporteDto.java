package com.barberia.modules.reports.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class CitaReporteDto {
    private final Integer id;
    private final LocalDate fecha;
    private final LocalTime hora;
    private final String clienteNombre;
    private final String barberoNombre;
    private final String estado;
    private final BigDecimal totalEstimado;
    private final Integer serviciosCount;
}
