package com.barberia.modules.commissions.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class DescuentoConsumoResponse {
    private final Long id;
    private final Integer barberoId;
    private final String barberoNombre;
    private final Integer productoId;
    private final String productoNombre;
    private final Integer cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal montoTotal;
    private final LocalDate fecha;
    private final String motivo;
    private final String usuarioRegistroNombre;
    private final Long corteId;
    private final LocalDateTime createdAt;
}
