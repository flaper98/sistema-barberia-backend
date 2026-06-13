package com.barberia.modules.finance.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class FinanzaResponse {

    private final Integer id;
    private final String tipo;
    private final String categoria;
    private final String descripcion;
    private final BigDecimal monto;
    private final LocalDate fecha;
    private final String usuarioNombre;
    private final String referencia;
    private final Integer ventaId;
    private final LocalDateTime createdAt;
}
