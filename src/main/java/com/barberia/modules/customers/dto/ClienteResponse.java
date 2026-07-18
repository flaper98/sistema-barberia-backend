package com.barberia.modules.customers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "Datos del cliente")
public class ClienteResponse {

    private final Integer id;
    private final String nombre;
    private final String apellido;
    private final String nombreCompleto;
    private final String correo;
    private final String telefono;
    private final LocalDate fechaNacimiento;
    private final String notas;
    private final Boolean estado;
    private final Integer cantidadSellos;
    private final Integer recompensasDisponibles;
    private final String descripcionRecompensa;
    private final LocalDate ultimaVisita;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
