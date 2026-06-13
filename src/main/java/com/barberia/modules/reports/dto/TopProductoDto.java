package com.barberia.modules.reports.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TopProductoDto {
    private final Integer productoId;
    private final String nombre;
    private final String categoria;
    private final Long unidadesVendidas;
    private final BigDecimal ingresos;
    private final Integer stockActual;
}
