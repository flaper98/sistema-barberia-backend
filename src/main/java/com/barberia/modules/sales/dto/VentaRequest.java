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

    // Sin @NotNull a proposito: cuando un BARBER crea una solicitud
    // pendiente no elige cliente ni metodo de pago -- eso lo asigna
    // recepcion al confirmar. VentaServiceImpl.crear() exige ambos solo
    // cuando quien crea NO es un barbero (venta directa COMPLETADA).
    private Integer clienteId;

    // Sin @NotNull a proposito: no toda venta la atiende un barbero (ej.
    // alguien compra o consume algo sin que sea por un servicio de
    // barberia) -- ver VentaServiceImpl.crear().
    private Integer barberoId;

    // Sin @NotEmpty a proposito: una solicitud de barbero llega sin pagos
    // todavia. Para una venta directa (COMPLETADA) es obligatoria y la suma
    // de los montos tiene que coincidir con el total -- ver
    // VentaServiceImpl.crear(). Puede traer mas de uno (ej. mitad efectivo,
    // mitad Yape).
    @Valid
    private List<PagoItemRequest> pagos;

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

    @Getter
    @Setter
    public static class PagoItemRequest {

        @NotNull(message = "El método de pago es obligatorio")
        private MetodoPago metodoPago;

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto tiene que ser mayor a 0")
        private BigDecimal monto;
    }
}
