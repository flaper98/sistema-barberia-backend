package com.barberia.modules.sales.dto;

import com.barberia.modules.sales.entity.MetodoPago;
import com.barberia.modules.sales.entity.TipoItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class VentaRequest {

    private Integer clienteId;

    @NotNull(message = "El ID del barbero es obligatorio")
    private Integer barberoId;

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;

    @DecimalMin(value = "0.00", message = "El descuento no puede ser negativo")
    private BigDecimal descuento = BigDecimal.ZERO;

    private String notas;

    private Integer citaId;

    @NotEmpty(message = "La venta debe tener al menos un ítem")
    @Valid
    private List<VentaItemRequest> items;

    @Getter
    @Setter
    public static class VentaItemRequest {

        @NotNull(message = "El tipo de ítem es obligatorio")
        private TipoItem tipo;

        @NotNull(message = "El ID del ítem es obligatorio")
        private Integer itemId;

        @NotBlank(message = "El nombre del ítem es obligatorio")
        private String nombre;

        @NotNull
        @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
        private BigDecimal precio;

        @NotNull
        @Min(value = 1, message = "La cantidad debe ser mayor a 0")
        private Integer cantidad;
    }
}
