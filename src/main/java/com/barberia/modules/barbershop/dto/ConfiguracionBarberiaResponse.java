package com.barberia.modules.barbershop.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ConfiguracionBarberiaResponse {

    private final String nombre;
    private final String direccion;
    private final String telefono;
    private final String email;
    private final String ruc;
    private final String horario;
    private final String logoUrl;
    private final String mensajeBienvenidaWhatsapp;
    private final String mensajeRecordatorioWhatsapp;
    private final LocalDateTime updatedAt;
}
