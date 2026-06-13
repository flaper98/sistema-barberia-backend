package com.barberia.modules.finance.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ResumenFinancieroResponse {

    private final BigDecimal totalIngresos;
    private final BigDecimal totalEgresos;
    private final BigDecimal utilidad;
    private final String periodo;
}
