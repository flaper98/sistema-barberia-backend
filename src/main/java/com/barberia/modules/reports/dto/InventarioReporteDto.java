package com.barberia.modules.reports.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InventarioReporteDto {
    private final Integer id;
    private final LocalDateTime fecha;
    private final String productoNombre;
    private final String tipoMovimiento;
    private final Integer cantidad;
    private final Integer stockAnterior;
    private final Integer stockNuevo;
    private final String motivo;
    private final String usuarioNombre;
}
