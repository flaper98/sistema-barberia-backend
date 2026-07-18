package com.barberia.modules.icloud.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** Nunca incluye la contraseña ni su forma cifrada — solo si ya hay una guardada. */
@Getter
@Builder
public class IcloudConfigResponse {
    private final String username;
    private final boolean passwordConfigured;
    private final boolean enabled;
    private final LocalDateTime updatedAt;
}
