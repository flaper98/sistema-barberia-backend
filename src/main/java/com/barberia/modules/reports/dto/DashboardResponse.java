package com.barberia.modules.reports.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DashboardResponse {

    private final Long ventasHoy;
    private final BigDecimal ingresosHoy;
    // Solo se llena para un BARBER (ver ReporteServiceImpl#getDashboard) --
    // un barbero no puede ver el total vendido, solo lo que le corresponde
    // de comision. Para el resto de los roles queda en null.
    private final BigDecimal comisionHoy;
    private final Long citasHoy;
    private final Long clientesAtendidos;
    private final Integer productosStockBajo;
    private final String barberoTopNombre;
    private final BigDecimal barberoTopMonto;
    private final Long recompensasPendientes;
}
