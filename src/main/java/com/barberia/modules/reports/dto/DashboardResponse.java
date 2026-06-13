package com.barberia.modules.reports.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class DashboardResponse {

    private final BigDecimal ingresosHoy;
    private final Long ventasHoy;
    private final Long citasHoy;
    private final Long citasPendientes;
    private final Integer productosStockBajo;
    private final Long clientesConRecompensa;

    private final BigDecimal ingresosDelMes;
    private final BigDecimal egresosDelMes;
    private final BigDecimal utilidadDelMes;

    private final List<BarberoResumenDto> topBarberos;

    @Getter
    @Builder
    public static class BarberoResumenDto {
        private final Integer id;
        private final String nombre;
        private final Long ventas;
        private final BigDecimal ingresos;
        private final BigDecimal comision;
    }
}
