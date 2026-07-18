package com.barberia.modules.reports.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DashboardResponse {

    private final Long ventasHoy;
    private final BigDecimal ingresosHoy;
    private final Long citasHoy;
    private final Long clientesAtendidos;
    private final Integer productosStockBajo;
    private final String barberoTopNombre;
    private final BigDecimal barberoTopMonto;
    private final Long recompensasPendientes;
}
