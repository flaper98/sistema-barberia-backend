package com.barberia.modules.services.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Se usa tanto para el request (solo productoId/cantidad hacen falta) como
// para la response (ahi tambien viene productoNombre, resuelto en el
// servidor, para que el frontend no tenga que cruzarlo con el catalogo).
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicioCortesiaDto {

    @NotNull(message = "El producto de cortesía es obligatorio")
    private Integer productoId;

    private String productoNombre;

    @NotNull(message = "La cantidad de cortesía es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;
}
