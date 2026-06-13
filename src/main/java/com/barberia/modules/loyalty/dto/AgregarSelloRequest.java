package com.barberia.modules.loyalty.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgregarSelloRequest {

    private Integer ventaId;

    @Size(max = 255)
    private String motivo;
}
