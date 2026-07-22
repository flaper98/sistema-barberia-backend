package com.barberia.modules.commissions.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class PropinaResponse {
    private final Long id;
    private final Integer barberoId;
    private final String barberoNombre;
    private final BigDecimal monto;
    private final LocalDate fecha;
    private final String notas;
    private final String usuarioRegistroNombre;
    private final Long corteId;
    private final LocalDateTime createdAt;
}
