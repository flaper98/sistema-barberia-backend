package com.barberia.modules.services.dto;

import com.barberia.modules.services.entity.CategoriaServicio;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ServicioRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    private String nombre;

    @NotNull(message = "La categoría es obligatoria")
    private CategoriaServicio categoria;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
    private BigDecimal precio;

    @NotNull(message = "La duración es obligatoria")
    @Min(value = 1, message = "La duración debe ser de al menos 1 minuto")
    private Integer duracionMinutos;

    @Size(max = 2000, message = "La descripción no puede superar 2000 caracteres")
    private String descripcion;
}
