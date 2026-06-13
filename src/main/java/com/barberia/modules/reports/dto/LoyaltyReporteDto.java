package com.barberia.modules.reports.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoyaltyReporteDto {
    private final Long totalClientesConCuenta;
    private final Long clientesConRecompensas;
    private final Long totalSellosOtorgados;
    private final Long totalRecompensasCanjeadas;
    private final Long totalRecompensasPendientes;
    private final Double promedioSellosPorCliente;
}
