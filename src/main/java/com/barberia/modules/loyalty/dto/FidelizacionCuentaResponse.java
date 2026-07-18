package com.barberia.modules.loyalty.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FidelizacionCuentaResponse {

    private final Integer id;
    private final Integer clienteId;
    private final String clienteNombre;
    private final Integer sellosActuales;
    private final Integer sellosNecesarios;
    private final Integer sellosParaProximaRecompensa;
    private final Integer recompensasDisponibles;
    private final Integer recompensasCanjeadas;
    private final Integer totalSellosAcumulados;
    private final String descripcionRecompensa;
    private final LocalDateTime ultimoMovimiento;
}
