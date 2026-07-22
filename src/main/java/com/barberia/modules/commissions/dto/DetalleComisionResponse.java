package com.barberia.modules.commissions.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Una fila del detalle "por item" del resumen de comision de un barbero --
// una venta puntual de un servicio/producto/paquete (con su fecha), no un
// agregado por categoria (ver ResumenComisionResponse).
@Getter
@Builder
public class DetalleComisionResponse {
    private final String tipo;
    private final Integer itemId;
    private final String nombre;
    private final Integer cantidad;
    private final BigDecimal base;
    private final BigDecimal comision;
    private final LocalDateTime fecha;
}
