package com.barberia.modules.workers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "Datos para crear o actualizar un barbero")
public class BarberoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    @Schema(example = "Marco")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede superar 100 caracteres")
    @Schema(example = "Antoniazzi")
    private String apellido;

    @Pattern(regexp = "^[0-9\\-\\+\\s()]{7,20}$", message = "El teléfono no tiene un formato válido")
    private String telefono;

    @Size(max = 200, message = "La especialidad no puede superar 200 caracteres")
    @Schema(example = "Corte y barba clásica")
    private String especialidad;

    @NotNull(message = "El porcentaje de comisión es obligatorio")
    @DecimalMin(value = "0.00", message = "El porcentaje de comisión no puede ser negativo")
    @DecimalMax(value = "100.00", message = "El porcentaje de comisión no puede superar 100")
    @Schema(example = "45.00")
    private BigDecimal porcentajeComision;

    @Size(max = 500)
    private String avatar;
}
