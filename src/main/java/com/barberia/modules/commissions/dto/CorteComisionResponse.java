package com.barberia.modules.commissions.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class CorteComisionResponse {
    private final Long id;
    private final Integer barberoId;
    private final String barberoNombre;
    private final LocalDate fechaDesde;
    private final LocalDate fechaHasta;
    private final BigDecimal baseServicios;
    private final BigDecimal baseProductos;
    private final BigDecimal comisionServicios;
    private final BigDecimal comisionProductos;
    private final BigDecimal totalPropinas;
    private final BigDecimal totalDescuentos;
    private final BigDecimal totalAPagar;
    private final String estado;
    private final LocalDateTime fechaPago;
    private final String notas;
    private final String usuarioRegistroNombre;
    private final LocalDateTime createdAt;
}
