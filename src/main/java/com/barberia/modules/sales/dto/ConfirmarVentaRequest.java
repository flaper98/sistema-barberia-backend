package com.barberia.modules.sales.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

// Aceptar y cobrar una solicitud PENDIENTE creada por un barbero: recepcion
// asigna (opcionalmente) el cliente y define como se pago -- puede ser mas
// de un metodo (ej. mitad efectivo, mitad Yape), ver VentaServiceImpl.confirmar().
@Getter
@Setter
public class ConfirmarVentaRequest {

    private Integer clienteId;

    @NotEmpty(message = "Los pagos son obligatorios para cobrar")
    @Valid
    private List<VentaRequest.PagoItemRequest> pagos;
}
