package com.barberia.modules.reports.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class VentaReporteDto {
    private final Integer id;
    private final LocalDateTime fecha;
    private final String clienteNombre;
    private final String barberoNombre;
    private final BigDecimal subtotal;
    private final BigDecimal descuento;
    private final BigDecimal total;
    private final String metodoPago;
    private final String estado;
    private final Integer itemsCount;
}
