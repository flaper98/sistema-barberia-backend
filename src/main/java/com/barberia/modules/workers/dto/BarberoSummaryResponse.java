package com.barberia.modules.workers.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class BarberoSummaryResponse {

    private final Integer barberoId;
    private final String nombreCompleto;
    private final BigDecimal porcentajeComision;
    private final Long totalVentas;
    private final BigDecimal ingresosGenerados;
    private final BigDecimal comisionCalculada;
    private final Long serviciosRealizados;
    private final String periodo;
}
