package com.barberia.modules.inventory.dto;

import com.barberia.modules.inventory.entity.TipoMovimiento;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovimientoRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    private Integer productoId;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    private TipoMovimiento tipoMovimiento;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @Size(max = 255, message = "El motivo no puede superar 255 caracteres")
    private String motivo;
}
