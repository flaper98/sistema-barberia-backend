package com.barberia.modules.reports.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class VentasPorBarberoDto {
    private final Integer barberoId;
    private final String barberoNombre;
    private final Long totalVentas;
    private final BigDecimal ingresos;
    private final BigDecimal comision;
    private final BigDecimal porcentajeComision;
    private final Long serviciosRealizados;
}
