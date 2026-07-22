package com.barberia.modules.notifications.dto;

import com.barberia.modules.notifications.entity.TipoNotificacion;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificacionResponse {
    private Long id;
    private TipoNotificacion tipo;
    private String mensaje;
    private String entidadTipo;
    private Integer entidadId;
    private boolean leida;
    private LocalDateTime fecha;
}
