package com.barberia.modules.inventory.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MovimientoResponse {

    private final Integer id;
    private final Integer productoId;
    private final String productoNombre;
    private final String tipoMovimiento;
    private final Integer cantidad;
    private final Integer stockAnterior;
    private final Integer stockNuevo;
    private final String motivo;
    private final String usuarioNombre;
    private final Integer ventaId;
    private final LocalDateTime createdAt;
}
