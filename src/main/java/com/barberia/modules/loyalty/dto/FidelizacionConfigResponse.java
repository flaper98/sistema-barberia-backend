package com.barberia.modules.loyalty.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FidelizacionConfigResponse {

    private final Integer id;
    private final Integer sellosNecesarios;
    private final Integer sellosPorGanancia;
    private final String descripcionRecompensa;
    private final Boolean activo;
    private final LocalDateTime updatedAt;
}
