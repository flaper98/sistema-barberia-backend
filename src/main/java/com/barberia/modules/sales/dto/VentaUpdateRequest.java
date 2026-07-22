package com.barberia.modules.sales.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Edicion de una venta ya registrada (solo ADMIN). Reemplaza tanto los datos
 * generales como la lista completa de items -- el subtotal/total/tipo de
 * venta se recalculan en el backend a partir de los items recibidos, igual
 * que en la creacion.
 */
@Getter
@Setter
public class VentaUpdateRequest {

    // Ninguno de los dos es obligatorio -- no toda venta tiene un cliente
    // registrado ni un barbero que la atendio (ver VentaServiceImpl).
    private Integer clienteId;

    private Integer barberoId;

    // La suma de los montos tiene que coincidir con el total recalculado a
    // partir de los items -- ver VentaServiceImpl.actualizar().
    @NotEmpty(message = "Los pagos son obligatorios")
    @Valid
    private List<VentaRequest.PagoItemRequest> pagos;

    @DecimalMin(value = "0.00", message = "El descuento no puede ser negativo")
    private BigDecimal descuento = BigDecimal.ZERO;

    private String notas;

    @NotEmpty(message = "La venta debe tener al menos un ítem")
    @Valid
    private List<VentaRequest.VentaItemRequest> items;
}
