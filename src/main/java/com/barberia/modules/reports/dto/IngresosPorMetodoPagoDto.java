package com.barberia.modules.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngresosPorMetodoPagoDto {
    private String metodoPago;
    private BigDecimal total;
    private Long cantidad;
    private BigDecimal porcentaje;
}
