package com.barberia.modules.appointments.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
public class CitaResponse {

    private final Integer id;
    private final Integer clienteId;
    private final String clienteNombre;
    private final Integer barberoId;
    private final String barberoNombre;
    private final LocalDate fecha;
    private final LocalTime hora;
    private final String estado;
    private final BigDecimal totalEstimado;
    private final String notas;
    private final String appointmentSource;
    private final String appointmentCode;
    private final Integer ventaId;
    private final List<CitaServicioItemResponse> servicios;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    @Getter
    @Builder
    public static class CitaServicioItemResponse {
        private final Integer id;
        private final Integer servicioId;
        private final String servicioNombre;
        private final BigDecimal precio;
        private final Integer duracionMinutos;
    }
}
