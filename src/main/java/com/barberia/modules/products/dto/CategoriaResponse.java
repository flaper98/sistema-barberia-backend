package com.barberia.modules.products.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CategoriaResponse {
    private final Integer id;
    private final String nombre;
    private final Boolean estado;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
