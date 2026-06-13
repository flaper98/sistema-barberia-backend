package com.barberia.modules.reports.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TopServicioDto {
    private final Integer servicioId;
    private final String nombre;
    private final String categoria;
    private final Long vecesVendido;
    private final BigDecimal ingresos;
    private final BigDecimal precioPromedio;
}
