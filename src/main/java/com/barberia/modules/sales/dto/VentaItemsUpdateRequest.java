package com.barberia.modules.sales.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

// Usado por el barbero para agregar/quitar items de su propia solicitud
// de venta mientras sigue PENDIENTE -- a diferencia de VentaUpdateRequest
// (solo ADMIN), este no permite tocar cliente, metodo de pago ni barbero.
@Getter
@Setter
public class VentaItemsUpdateRequest {

    @NotEmpty(message = "La venta debe tener al menos un ítem")
    @Valid
    private List<VentaRequest.VentaItemRequest> items;
}
